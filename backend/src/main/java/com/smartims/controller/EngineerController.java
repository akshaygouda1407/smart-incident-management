package com.smartims.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engineer")
public class EngineerController {

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ENGINEER')")
    public String engineerTasks() {
        return "Engineer tasks access granted 🛠️";
    }
}
