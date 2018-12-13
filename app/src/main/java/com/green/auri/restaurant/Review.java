package com.green.auri.restaurant;

public class Review {
    private String author;
    private String date;
    private String review;

    public Review(String author, String date, String review) {
        this.author = author;
        this.date = date;
        this.review = review;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getReview() {
        return review;
    }
}
