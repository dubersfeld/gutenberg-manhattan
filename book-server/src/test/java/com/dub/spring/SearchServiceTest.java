package com.dub.spring;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.Category;
import com.dub.spring.services.SearchService;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
public class SearchServiceTest {

	@Autowired
	SearchService searchService;
	
	
	@Test
	void testSearchByTags() {
		
		Flux<Book> books = searchService.searchByTags("biography system");
		StepVerifier.create(books.log())
		.expectNextCount(6)
		.verifyComplete();			
	}
	
	
	@Test
	void testSearchByTitle() {
		
		Flux<Book> books = searchService.searchByTitle("Wrath Life");
		StepVerifier.create(books.log())
		.expectNextCount(3)
		.verifyComplete();
		
	}
	

	@Test
	void testSearchByDescription() {
		
		Flux<Book> books = searchService.searchByDescription("gorilla quantum pattern captain");
		StepVerifier.create(books.log())
		.expectNextCount(5)
		.verifyComplete();
		
	}
	
	
}
