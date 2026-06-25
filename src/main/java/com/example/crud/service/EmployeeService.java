package com.example.crud.service;

import org.springframework.stereotype.Service;

import com.example.crud.dto.EmployeeRequestDto;
import com.example.crud.entity.Employee;
import com.example.crud.repository.EmployeeRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmployeeService {
	
	private EmployeeRepository repo;
	
	public Employee create(EmployeeRequestDto em) {
		Employee e = new Employee();
		e.setName(em.getName());
		e.setEmail(em.getEmail());
		e.setSalary(em.getSalary());
		
		return repo.save(e);
	}
}
