# Introduction

This is a poorly written application, and we're expecting the candidate to greatly improve this code base.

## Instructions
* **Consider this to be your project! Feel free to make any changes**
* There are several deliberate design, code quality and test issues in the current code, they should be identified and resolved
* Implement the "New Requirements" below
* Keep it mind that code quality is very important
* Focus on testing, and feel free to bring in any testing strategies/frameworks you'd like to implement
* You're welcome to spend as much time as you like, however, we're expecting that this should take no more than 2 hours

## `movie-theater`

### Current Features
* Customer can make a reservation for the movie
  * And, system can calculate the ticket fee for customer's reservation
* Theater have a following discount rules
  * 20% discount for the special movie
  * $3 discount for the movie showing 1st of the day
  * $2 discount for the movie showing 2nd of the day
* System can display movie schedule with simple text format

## New Requirements
* New discount rules; In addition to current rules
  * Any movies showing starting between 11AM ~ 4pm, you'll get 25% discount
  * Any movies showing on 7th, you'll get 1$ discount
  * The discount amount applied only one if met multiple rules; biggest amount one
* We want to print the movie schedule with simple text & json forma


## Solution
* Rearranged code to make Showing the central theme since in real world, reservations are made for a Showing - not a movie
* I prefer Immutability - incorporated Lombok and @Value. This has also reduced boilerplate.
* We can perhaps optimize by precomputing sequence and ticket prices (instead of computing on demand). Goal is correct functionality first
* A LOT of tests can be written. I would employ generative testing (property based Tests) instead of manually writing various combinations. It's not done as part of the exercise.
* Introduced reasonable bounds on show time, ticket prices, Duration and title
* Changed to Enum based Singleton as recommended in Effective Java Book
* Turned Reservations method as thread safe. This can be further optimized but we are going for correctness first. 