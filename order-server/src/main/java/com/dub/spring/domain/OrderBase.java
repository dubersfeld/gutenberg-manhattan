package com.dub.spring.domain;

import java.io.Serializable;
import java.util.ArrayList;

//java.lang.Object
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;


public class OrderBase implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2623188979726019146L;

	@Id
	private String id;
	
	private OrderState state;
	private List<Item> lineItems;
	private Address shippingAddress;
	private PaymentMethod paymentMethod;
	private int subtotal;
	private LocalDateTime date;
	
	public OrderBase() {
		this.lineItems = new ArrayList<>();
		this.shippingAddress = new Address();
		this.paymentMethod = new PaymentMethod();
	}
	
	public OrderBase(OrderBase that) {
		this.id = that.id;
		this.state = that.state;
		this.lineItems = that.lineItems;
		this.shippingAddress = that.shippingAddress;
		this.paymentMethod = that.paymentMethod;
		this.subtotal = that.subtotal;
		this.date = that.date;
	}

	
	public int getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(int subtotal) {
		this.subtotal = subtotal;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setState(OrderState state) {
		this.state = state;
	}

	public OrderState getState() {
		return state;
	}


	public List<Item> getLineItems() {
		return lineItems;
	}


	public void setLineItems(List<Item> lineItems) {
		this.lineItems = lineItems;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}


	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}


	public LocalDateTime getDate() {
		return date;
	}


	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	
}
