package com.ecom.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Corrected methods to search by the 'name' property of the 'Category' entity
    // This finds products where 'active' is true AND the category's name matches the given string.
    List<Product> findByActiveTrueAndCategoryName(String categoryName);

    // For pagination, if you want to filter by active and category name
    Page<Product> findByActiveTrueAndCategoryName(Pageable pageable, String categoryName);

    // This method name seems to imply searching by title OR category name.
    // Ensure 'Category' in the method name correctly maps to 'category.name' if that's the intent.
    // If 'Category' in the method name is meant to search directly on a String category field in Product,
    // and your Product's 'category' field is an object, this will still be an issue.
    // Assuming `category` in `OrCategoryContainingIgnoreCase` should refer to `category.name`:
    Page<Product> findByActiveTrueAndTitleContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
            String titleSearch, String categoryNameSearch, Pageable pageable);

                Page<Product> findByActiveTrue(Pageable pageable);

    // Keep existing correct methods
    List<Product> findAllByCategory_NameAndActiveTrue(String name);
    List<Product> findAllByActiveTrue();
    // No longer needed if using `findByActiveTrueAndCategoryName` for general cases:
    // List<Product> findByCategoryName(String categoryName);
}