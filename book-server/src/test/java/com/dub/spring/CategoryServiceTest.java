package com.dub.spring;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dub.spring.domain.Category;
import com.dub.spring.services.CategoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class CategoryServiceTest {

	@Autowired
	CategoryService categoryService;
	
	private Predicate<Category> pred1 = 
			cat -> {
				System.err.println(cat.getSlug().length());
				boolean match = "books".equals(cat.getSlug());
			
				
				return match; };
	
	private Predicate<Category> pred2 = 
			cat -> {
				System.err.println(cat.getSlug().length());
				boolean match = "textbooks".equals(cat.getSlug());
						
				return match; };		
			
	private Predicate<Category> pred3 = 
			cat -> {
				System.err.println(cat.getSlug().length());
				boolean match = "biographies".equals(cat.getSlug());
				
				return match; };		
			
	private Predicate<Category> pred4 = 
			cat -> {
				System.err.println(cat.getSlug());
				boolean match = "computer-science".equals(cat.getSlug());
						
				return match; };		
		
	private Predicate<Category> pred5 = 
			cat -> {
				System.err.println(cat.getSlug());
				boolean match = "fiction".equals(cat.getSlug());
				
				return match; };	
						
	private Predicate<Category> testBySlug = 
			cat -> {
				System.err.println(cat.getId());
				boolean match = "59fd6b39acc04f10a07d1344".equals(cat.getId());	
					
				return match; };
						
			
	@Test
	void testFindAll() {
	     Flux<Category> cats = categoryService.findAllCategories();
  
	     StepVerifier.create(cats.log())
	     			.expectNextMatches(pred1)
	     			.expectNextMatches(pred2)			
	     			.expectNextMatches(pred3)
	     			.expectNextMatches(pred4)	     			
	     			.expectNextMatches(pred5)
	     			.expectComplete()
	     			.verify();     
	}
	
	
	@Test
	void testBySlug() {
		Mono<Category> cat = categoryService.getCategory("fiction");
	
		StepVerifier.create(cat.log())
					.expectNextMatches(testBySlug)
					.verifyComplete();
	}
	
}
