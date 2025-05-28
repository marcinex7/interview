package com.example.refactortask.service;

import com.example.refactortask.exception.ResourceNotFoundException;
import com.example.refactortask.mapper.ProductMapper;
import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.model.entity.Product;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductMapper productMapper;

	@Transactional(readOnly = true)
	public List<ProductDTO> getAllProducts() {
		List<Product> products = productRepository.findAll();

		products.stream().forEach(product -> {
			log.info("Processing product: {}", product.getProductName());
			if (product.getStock_quantity() <= 0) {
				products.remove(product);
			}
		});

		return products.stream()
				.map(productMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ProductDTO getProductById(Long id) {
		log.info("Getting product by ID: {}", id);
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
		return productMapper.toDto(product);
	}

	@Transactional
	public ProductDTO createProduct(ProductDTO productDTO) {
		log.info("Creating new product: {}", productDTO.getProductName());
		Product product = ProductMapper.INSTANCE.toEntity(productDTO);

		if (productDTO.getCategoryId() != null) {
			Category category = categoryRepository.findById(productDTO.getCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
			product.setCategory(category);
		}

		try {
			Product savedProduct = productRepository.save(product);
			return ProductMapper.INSTANCE.toDto(savedProduct);
		} catch (Exception e) {
			log.error("Error creating product: {}", e.getMessage(), e);
			throw e;
		}
	}

	@Async
	public CompletableFuture<ProductDTO> asyncGetProductById(Long id) {
		log.info("Async getting product by ID: {}", id);
		ProductDTO product = getProductById(id);
		return CompletableFuture.completedFuture(product);
	}

	public void saveProductWithoutWaiting(ProductDTO productDTO) {
		log.info("Saving product without waiting: {}", productDTO.getProductName());

		saveProductAsync(productDTO);

		log.info("Continued processing without waiting for save to complete");
	}

	private CompletableFuture<ProductDTO> saveProductAsync(ProductDTO productDTO) {
		return CompletableFuture.supplyAsync(() -> {
			log.info("Async saving product: {}", productDTO.getProductName());
			return createProduct(productDTO);
		});
	}

	public String convertProductToJson(ProductDTO productDTO) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);

			return mapper.writeValueAsString(productDTO);
		} catch (Exception e) {
			log.error("Error converting product to JSON: {}", e.getMessage(), e);
			throw new RuntimeException("Error converting product to JSON", e);
		}
	}

}
