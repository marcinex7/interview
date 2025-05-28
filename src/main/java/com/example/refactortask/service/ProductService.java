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

		// BAD PRACTICE: Modifying the source collection during stream processing
		// This will throw ConcurrentModificationException
		products.stream().forEach(product -> {
			log.info("Processing product: {}", product.getProductName());
			if (product.getStock_quantity() <= 0) {
				// This modification during iteration will cause ConcurrentModificationException
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

	/**
	 * This method demonstrates a common mistake: calling a @Transactional method from within the same class.
	 * The @Transactional annotation works through Spring's proxy mechanism, which means that when a method
	 * is called from within the same class, the proxy is bypassed and the transaction doesn't work as expected.
	 */
	public ProductDTO getProductByIdWithLogging(Long id) {
		log.info("About to get product with ID: {}", id);

		// WRONG PRACTICE: Self-invocation of a @Transactional method
		// The transaction won't be applied because we're bypassing the Spring proxy
		ProductDTO product = this.getProductById(id);

		log.info("Retrieved product: {}", product.getProductName());
		return product;
	}

	@Transactional
	public ProductDTO createProduct(ProductDTO productDTO) {
		log.info("Creating new product: {}", productDTO.getProductName());
		Product product = productMapper.toEntity(productDTO);

		// Manually setting the category (inconsistent with mapper)
		if (productDTO.getCategoryId() != null) {
			Category category = categoryRepository.findById(productDTO.getCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
			product.setCategory(category);
		}

		try {
			// Intentionally logging and rethrowing exceptions to demonstrate bad practice
			Product savedProduct = productRepository.save(product);
			return productMapper.toDto(savedProduct);
		} catch (Exception e) {
			log.error("Error creating product: {}", e.getMessage(), e);
			throw e; // Bad practice: rethrowing without adding context
		}
	}

	/**
	 * This method demonstrates another common mistake: using @Async on a method that's called from within the same class.
	 * Similar to @Transactional, @Async works through Spring's proxy mechanism, which means that when a method
	 * is called from within the same class, the proxy is bypassed and the method runs synchronously.
	 */
	@Async
	public CompletableFuture<ProductDTO> asyncGetProductById(Long id) {
		log.info("Async getting product by ID: {}", id);
		ProductDTO product = getProductById(id);
		return CompletableFuture.completedFuture(product);
	}

	/**
	 * This method demonstrates a bad practice of not properly handling CompletableFuture results.
	 * The saveProductAsync method is called but its result is ignored, which means if there's an error,
	 * it will be silently ignored and the data won't be saved.
	 */
	public void saveProductWithoutWaiting(ProductDTO productDTO) {
		log.info("Saving product without waiting: {}", productDTO.getProductName());

		// BAD PRACTICE: Not joining or getting the result of the CompletableFuture
		// If there's an error, it will be silently ignored
		saveProductAsync(productDTO);

		log.info("Continued processing without waiting for save to complete");
	}

	/**
	 * Asynchronously saves a product.
	 */
	private CompletableFuture<ProductDTO> saveProductAsync(ProductDTO productDTO) {
		return CompletableFuture.supplyAsync(() -> {
			log.info("Async saving product: {}", productDTO.getProductName());
			return createProduct(productDTO);
		});
	}

	/**
	 * This method demonstrates a bad practice: creating a new ObjectMapper instance for each method call.
	 * ObjectMapper is expensive to create and should be reused as a singleton bean.
	 */
	public String convertProductToJson(ProductDTO productDTO) {
		try {
			// BAD PRACTICE: Creating a new ObjectMapper instance for each method call
			// This is inefficient and wastes resources
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);

			return mapper.writeValueAsString(productDTO);
		} catch (Exception e) {
			log.error("Error converting product to JSON: {}", e.getMessage(), e);
			throw new RuntimeException("Error converting product to JSON", e);
		}
	}

}
