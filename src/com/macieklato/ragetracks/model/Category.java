package com.macieklato.ragetracks.model;

public class Category {
	private String slug;
	private String name;

	public Category() {

	}

	public Category(String name, String slug) {
		setName(name);
		setSlug(slug);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getName() {
		return name;
	}

	public String getSlug() {
		return slug;
	}
}
