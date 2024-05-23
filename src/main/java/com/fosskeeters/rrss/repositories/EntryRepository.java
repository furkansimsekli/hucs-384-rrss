package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Entry;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryRepository extends JpaRepository<Entry, Long> {}
