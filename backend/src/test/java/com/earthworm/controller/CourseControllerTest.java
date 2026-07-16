package com.earthworm.controller;

import com.earthworm.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerTest {
    private CourseService courseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService)).build();
    }

    @Test
    void getCoursePacks_shouldReturn200() throws Exception {
        when(courseService.findCoursePacks()).thenReturn(List.of());

        mockMvc.perform(get("/course-pack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
