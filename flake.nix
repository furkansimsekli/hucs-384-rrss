{
  description = "";

  inputs.nixpkgs.url = "github:nixos/nixpkgs";

  outputs = { self, nixpkgs }: let
    forAllSystems = nixpkgs.lib.genAttrs [ "aarch64-linux" "x86_64-linux" "aarch64-darwin" "x86_64-darwin" ];
  in
  rec {
    packages = forAllSystems (system: let
      pkgs = nixpkgs.legacyPackages.${system};
      deps = pkgs.stdenv.mkDerivation {
        pname = "RRSS-deps";
        version = "0.0";
        src = ./.;

        nativeBuildInputs = [ pkgs.gradle pkgs.perl ] ++ pkgs.lib.optional pkgs.stdenv.isDarwin pkgs.xcbuild;

        buildPhase = ''
            export GRADLE_USER_HOME=$(mktemp -d)
            gradle --no-daemon -x test build
        '';

        # perl code mavenizes pathes (com.squareup.okio/okio/1.13.0/a9283170b7305c8d92d25aff02a6ab7e45d06cbe/okio-1.13.0.jar -> com/squareup/okio/okio/1.13.0/okio-1.13.0.jar)
      installPhase = ''
        find $GRADLE_USER_HOME -type f -regex '.*/modules.*\.\(jar\|pom\)' \
          | perl -pe 's#(.*/([^/]+)/([^/]+)/([^/]+)/[0-9a-f]{30,40}/([^/\s]+))$# ($x = $2) =~ tr|\.|/|; "install -Dm444 $1 \$out/$x/$3/$4/$5" #e' \
          | sh
        rm -rf $out/tmp
      '';

      outputHashAlgo = "sha256";
      outputHashMode = "recursive";
      outputHash = "sha256-XEU1vf1wXJouud3ny9wWd9gE5Xb4I5tI9f1fDCuwSt4=";
    };
    in
    {
      default = pkgs.stdenv.mkDerivation {
        name = "RRSS";
        version = "0.1";
        src = ./.;

        nativeBuildInputs = with pkgs; [ gradle openjdk21 makeWrapper ];

        patchPhase = ''
          runHook prePatch

          sed -i '/docker-compose/d' build.gradle
          substituteInPlace build.gradle --replace 'mavenCentral()' 'mavenLocal() maven { url uri("${deps}") }'
          mkdir -p ../.m2/
          ln -s ${deps} ../.m2/repository

          runHook postPatch
        '';

        buildPhase = ''
          runHook preBuild

          echo "***This does not build as Gradle is a 'fast, dependable, and adaptable open-source build automation tool with an elegant and extensible declarative build language.'***"
          false
          export M2_HOME="${deps}"
          gradle --no-daemon -x test build

          runHook postBuild
        '';

        installPhase = ''
          runHook preInstall

          mkdir -p $out/lib $out/bin
          cp -r build/libs/*.jar $out/lib/rrss.jar
          makeWrapper ${pkgs.jre}/bin/java $out/bin/rrss

          runHook postInstall
        '';
      };
    });

    devShells = forAllSystems (system: let
      pkgs = nixpkgs.legacyPackages.${system};
    in
      {
        default = pkgs.mkShell {
          nativeBuildInputs = packages.${system}.default.nativeBuildInputs ++ [ pkgs.clang-tools ];
        };
      }
    );

    checks = forAllSystems (system: let
      pkgs = nixpkgs.legacyPackages.${system};
    in
      rec {
        default = rrss-java-fmt;

        rrss-java-fmt = pkgs.runCommand "rrss-java-fmt" { src = self; buildInputs = [ pkgs.clang-tools ]; } ''
          mkdir -p $out
          cp -r $src/* $out
          chmod -R u+w $out
          JAVA_FILES="$(find $out/src -name '*.java')"
          if ! clang-format -n --Werror --style=file $JAVA_FILES; then
            clang-format --style=file -i $JAVA_FILES
            for f in $JAVA_FILES; do
              diff --color=always -u $(sed "s|$out|$src|" <<< "$f") "$f" || true
            done
            exit 1
          fi
        '';
      }
    );
  };
}
