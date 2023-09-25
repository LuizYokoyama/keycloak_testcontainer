package io.github.LuizYokoyama.keycloak_testcontainer.dto;

public class DtoTest {
    private String userName;

    public DtoTest(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
