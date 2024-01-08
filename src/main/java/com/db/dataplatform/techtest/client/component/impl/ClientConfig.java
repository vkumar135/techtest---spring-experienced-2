package com.db.dataplatform.techtest.client.component.impl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Component
@ConfigurationProperties("client")
@Validated
@Data
public class ClientConfig
{
    @NotBlank
    public String url;

    @NotBlank
    private String bearerToken;
}