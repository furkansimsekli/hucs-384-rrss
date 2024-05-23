package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Topic;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {}
