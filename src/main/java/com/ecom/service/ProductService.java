package com.ecom.service;

import com.ecom.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
        Product saveProduct(Product product);
        List<Product> getAllProducts();
        public Boolean deleteProduct(Integer id);
        public Product getProductById(Integer id);

        public List<Product> getAllActiveProducts(String category);
        Product updateProduct(Product product, MultipartFile image);

        public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch);

        public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String categor);

}