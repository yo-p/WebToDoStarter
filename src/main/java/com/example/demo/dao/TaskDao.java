package com.example.demo.dao;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.Task;

public interface TaskDao{

	List<Task> findAll();

	Optional<Task> findById(int id);

	void insert(Task task);

	int update(Task task);

	int deleteById(int id);

	List<Task> findByType(int typeId);

}