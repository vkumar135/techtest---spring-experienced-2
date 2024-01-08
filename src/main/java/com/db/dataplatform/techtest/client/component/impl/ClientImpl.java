package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.api.model.DataBody;
import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.api.model.DataHeader;
import com.db.dataplatform.techtest.client.component.Client;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client code does not require any test coverage
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client
{

    public static final String URI_PUSHDATA = "/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("/update/{name}/{newBlockType}");

    private final RestTemplate restTemplate;
    private final ClientConfig clientConfig;
    private final HttpHeaders headers;

    @Autowired
    public ClientImpl(RestTemplateBuilder builder, ClientConfig clientConfig)
    {
        this.restTemplate = builder.build();
        this.clientConfig = clientConfig;
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + this.clientConfig.getBearerToken());
    }

    @Override
    public void pushData(DataEnvelope dataEnvelope)
    {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        HttpEntity<?> request = new HttpEntity<DataEnvelope>(dataEnvelope, this.headers);
        try
        {
            final ResponseEntity<JsonNode> response =
                    restTemplate.exchange(
                            clientConfig.getUrl() + URI_PUSHDATA, HttpMethod.POST, request, JsonNode.class);
            if (response.getStatusCode().equals(HttpStatus.OK))
            {
                log.info("Data {} to {} pushed!", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
            }

        }
        catch (HttpStatusCodeException e)
        {
            log.error("HttpStatusCodeException {}", e.getResponseBodyAsString());
        }
        catch (RestClientException e)
        {
            log.error("RestClientException {}", e.getMessage());
        }
    }

    @Override
    public List<DataEnvelope> getData(String blockType)
    {
        log.info("Query for data with header block type {}", blockType);
        HttpEntity request = new HttpEntity(headers);

        try
        {
            ResponseEntity<DataBodyEntity[]> response =
                    restTemplate.exchange(
                            clientConfig.getUrl() + URI_GETDATA,
                            HttpMethod.GET,
                            request,
                            DataBodyEntity[].class,
                            blockType);

            if (response.getStatusCode() == HttpStatus.OK)
            {
                DataBodyEntity[] dataArray = response.getBody();
                List<DataBodyEntity> dataBodyEntities =
                        Arrays.stream(dataArray).collect(Collectors.toList());
                List<DataEnvelope> envelopes =
                        dataBodyEntities.stream()
                                .map(
                                        dataBody ->
                                                new DataEnvelope(
                                                        new DataHeader(
                                                                dataBody.getDataHeaderEntity().getName(),
                                                                dataBody.getDataHeaderEntity().getBlocktype()),
                                                        new DataBody(dataBody.getDataBody())))
                                .collect(Collectors.toList());

                return envelopes;
            }
            else if (response.getStatusCode() == HttpStatus.NO_CONTENT)
            {
                log.info("block type {} Not found!", blockType);
            }
            else
            {
                log.info("block type {} Not found!", blockType);
            }
        }
        catch (HttpStatusCodeException e)
        {
            log.error("HttpStatusCodeException {}", e.getResponseBodyAsString());
        }
        catch (RestClientException e)
        {
            log.error("RestClientException {}", e.getMessage());
        }
        catch (Exception ex)
        {
            log.error("Exception {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateData(String blockName, String newBlockType)
    {
        log.info("Updating blocktype to {} for block with name {}", newBlockType, blockName);
        HttpEntity request = new HttpEntity(headers);

        try
        {
            ResponseEntity<Boolean> response =
                    restTemplate.exchange(
                            clientConfig.getUrl() + URI_PATCHDATA,
                            HttpMethod.PATCH,
                            request,
                            Boolean.class,
                            blockName,
                            newBlockType);

            if (response.getStatusCode() == HttpStatus.ACCEPTED)
            {
                log.info("Updated blocktype to {} for block with name {}", newBlockType, blockName);
                return true;
            }
            else if (response.getStatusCode() == HttpStatus.NO_CONTENT)
            {
                log.info("block name {} Not found!", blockName);
            }
            else
            {
                log.info("block name {} Not found!", blockName);
            }
        }
        catch (HttpStatusCodeException e)
        {
            log.error("HttpStatusCodeException {}", e.getResponseBodyAsString());
        }
        catch (RestClientException e)
        {
            log.error("RestClientException {}", e.getMessage());
        }
        catch (Exception ex)
        {
            log.error("Exception {}", ex.getMessage());
        }
        return false;
    }
}
