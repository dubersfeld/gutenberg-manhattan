package com.dub.spring;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.BookSearch;
import com.dub.spring.web.BookHandler;
import com.dub.spring.web.BookRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class BookHandlerTest {
	
	@Autowired
	BookRouter bookRouter;
	
	@Autowired
	BookHandler bookHandler;
	
	private Predicate<Book> testBoughtWith = 
			book -> {
				System.err.println(book.getSlug());
				boolean match = "raiders-pattern-3190".equals(book.getSlug());
					
				return match; 
	};	
	
	
	@Test
	void testBookBySlug() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/books/emerald-ultimate-421")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.id").isEqualTo("5a28f2b0acc04f7f2e9740a1")
		.jsonPath("$.title").isEqualTo("The Ultimate Emerald Reference");
	}
	
	
	@Test
	void testAllBooksByCategory() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/books/fiction/sort/ASC")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(5)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void testBookByid() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/bookById/5a28f2b0acc04f7f2e9740a1")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.slug").isEqualTo("emerald-ultimate-421")
		.jsonPath("$.title").isEqualTo("The Ultimate Emerald Reference");
	}
	
	
	@Test
	void testBooksBoughtWith() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/booksBoughtWith/5a28f2b0acc04f7f2e9740a1/outLimit/10")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextMatches(testBoughtWith)
		.expectNextCount(2)
		.expectComplete()
		.verify();
			
	}	
	

	@Test
	void searchByTitle() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("Wrath Life");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByTitle")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(3)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void searchByDescription() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("gorilla quantum pattern captain");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByDescription")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(5)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void searchByTags() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("biography system");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByTags")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(6)
		.expectComplete()
		.verify();	
	}	

}
