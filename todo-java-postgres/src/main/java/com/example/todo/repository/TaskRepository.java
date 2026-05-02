package com.example.todo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todo.model.Task;
import com.example.todo.model.Task.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);
    
    // Find tasks by title containing (case-insensitive search)
    List<Task> findByTitleContainingIgnoreCase(String title);
}

// Made with Bob
