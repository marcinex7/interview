package com.example.refactortask.client;

import com.example.refactortask.model.dto.ExternalProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FakeStoreApiClient {

    @Value("${external.api.fakestore.url:https://fakestoreapi.com}")
    private String apiBaseUrl;
    
    public List<ExternalProductDTO> getAllProducts() {
        try {
            String url = apiBaseUrl + "/products";
            ExternalProductDTO[] products = new RestTemplate().getForObject(url, ExternalProductDTO[].class);
            return products != null ? Arrays.asList(products) : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Error fetching products from external API", e);
            return Collections.emptyList();
        }
    }

    public Optional<ExternalProductDTO> getProductById(Integer id) {
        try {
            String url = apiBaseUrl + "/products/" + id;
            ExternalProductDTO product = new RestTemplate().getForObject(url, ExternalProductDTO.class);
            return Optional.ofNullable(product);
        } catch (RestClientException e) {
            log.error("Error fetching product with ID {} from external API", id, e);
            return Optional.empty();
        }
    }
}