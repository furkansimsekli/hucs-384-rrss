package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.ProductDto;
import com.fosskeeters.rrss.dtos.ReviewDto;
import com.fosskeeters.rrss.dtos.ReviewReplyDto;
import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.ProductImage;
import com.fosskeeters.rrss.models.Review;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.ProductImageRepository;
import com.fosskeeters.rrss.repositories.ProductRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/products/")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository productRepository,
                             ProductImageRepository productImageRepository,
                             UserRepository userRepository) throws IOException {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productImageRepository = productImageRepository;

        // FIXME: This should be variable instead of hardcoded.
        Files.createDirectories(Paths.get("public", "product-images"));
    }

    @GetMapping("/{id}")
    public String productGetHandler(HttpSession session, @PathVariable long id, Model model) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        User user = null;
        Review review = null;
        List<ReviewReplyDto> reviewReplyDtoList = new ArrayList<>();
        if (session.getAttribute("username") != null) {
            review = product.get()
                             .getReviews()
                             .stream()
                             .filter(r
                                     -> r.getAuthor().getUsername().equals(
                                             session.getAttribute("username")))
                             .findFirst()
                             .orElse(null);

            Optional<User> userOpt =
                    userRepository.findByUsername((String) session.getAttribute("username"));
            user = userOpt.isPresent() ? userOpt.get() : null;

            if (user != null && product.get().getOwner().getId() == user.getId()) {
                reviewReplyDtoList.addAll(
                        product.get().getReviews().stream().map(ReviewReplyDto::new).toList());
            }
        }
        model.addAttribute("isCustomer",
                           user != null ? user.getType() == User.Type.CUSTOMER : false);
        model.addAttribute("isProductOwner",
                           user != null ? user.getId() == product.get().getOwner().getId() : false);
        model.addAttribute("reviewReplyDtoList",
                           !reviewReplyDtoList.isEmpty() ? reviewReplyDtoList : null);
        model.addAttribute("product", product.get());
        model.addAttribute("reviewId", review != null ? review.getId() : null);
        model.addAttribute(
                "reviewDto",
                review != null ? new ReviewDto(review) : new ReviewDto(product.get().getId()));
        model.addAttribute("hasErrors", false);
        return "product";
    }

    @GetMapping("/create")
    public String getProductCreateForm(HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        Optional<User> user =
                userRepository.findByUsername(session.getAttribute("username").toString());

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (user.get().getType() != User.Type.MERCHANT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("productDto", new ProductDto());
        return "merchants/create_product";
    }

    @PostMapping("/create")
    public String createProduct(HttpSession session, @Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) throws IOException {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "merchants/create_product";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (user.get().getType() != User.Type.MERCHANT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Product product = new Product(user.get(), productDto);
        productRepository.save(product);

        if (!productDto.getImages().isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            int displayOrder = 1;

            for (MultipartFile image : productDto.getImages()) {
                if (image.isEmpty()) continue;

                String storageFileName = LocalDateTime.now() + "_" + image.getOriginalFilename();
                String storagePathStr = "/product-images/" + storageFileName;

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get("public" + storagePathStr),
                               StandardCopyOption.REPLACE_EXISTING);
                }

                ProductImage productImage =
                        new ProductImage(product, displayOrder, "", storagePathStr);
                productImages.add(productImage);
                displayOrder++;
            }

            productImageRepository.saveAll(productImages);
        }

        return "redirect:/merchants/" + username;
    }

    @GetMapping("/{productId}/update")
    public String getUpdateProductForm(HttpSession session, @PathVariable long productId,
                                       Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        Optional<User> user =
                userRepository.findByUsername(session.getAttribute("username").toString());
        Optional<Product> product = productRepository.findById(productId);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (product.get().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ProductDto productDto = new ProductDto(product.get());
        model.addAttribute("productDto", productDto);
        return "merchants/update_product";
    }

    @PostMapping("/{productId}/update")
    public String updateProduct(HttpSession session, @PathVariable long productId,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) throws IOException {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "merchants/update_product";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Product> product = productRepository.findById(productId);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (product.get().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!productDto.getImages().isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            int displayOrder = 1;

            for (MultipartFile image : productDto.getImages()) {
                if (image.isEmpty()) continue;

                String storageFileName = LocalDateTime.now() + "_" + image.getOriginalFilename();
                String storagePathStr = "/product-images/" + storageFileName;

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get("public" + storagePathStr),
                               StandardCopyOption.REPLACE_EXISTING);
                }

                ProductImage productImage =
                        new ProductImage(product.get(), displayOrder, "", storagePathStr);
                productImages.add(productImage);
                displayOrder++;
            }

            productImageRepository.deleteAllByProduct(product.get());
            productImageRepository.saveAll(productImages);
        }

        product.get().setFromProductDto(productDto);
        productRepository.save(product.get());
        return "redirect:/merchants/" + username;
    }

    @GetMapping("/{productId}/delete")
    public String deleteProduct(HttpSession session, @PathVariable long productId) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Product> product = productRepository.findById(productId);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (product.get().getOwner().getId() != user.get().getId()
            && user.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        productRepository.delete(product.get());
        return "redirect:/merchants/" + product.get().getOwner().getUsername();
    }
}
