package io.github.LuizYokoyama.keycloak_testcontainer.controller;

import io.github.LuizYokoyama.keycloak_testcontainer.dto.DtoTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerTest {

    @GetMapping("name")
    DtoTest getName(){
        return new DtoTest("userName Test Ok");
    }
}
