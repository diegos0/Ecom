package com.ecom.controller;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CategoryRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private final CategoryService categoryService;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Autowired
    public AdminController(CategoryService categoryService,
                           ProductService productService,
                           CategoryRepository categoryRepository,
                           UserService userService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    @ModelAttribute
    public void getUserDetails(Principal p, Model model){
        if(p!=null){
            String email = p.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user", userDtls);
        }

    }

    @GetMapping("/")
    public String index(){
        return "admin/index";
    }

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model m ){
        List<Category> categories = categoryService.getAllCategory();
        m.addAttribute("category", categories);  // Evitar errores en la vista si usas paginación compartida (como en layout base o fragmentos)

        return "admin/add_product";
    }

    @GetMapping("/category")
    public String category(Model m) {
        List<Category> categories = categoryService.getAllCategory();
        if (categories == null) {
            categories = new ArrayList<>(); // lista vacía para evitar null
        }
        m.addAttribute("categorys", categories);
        m.addAttribute("pageNo", 0); // inicializar página
        m.addAttribute("totalPages", 1); // si no usas paginación real, al menos 1
        m.addAttribute("isFirst", true);
        m.addAttribute("isLast", true);
        m.addAttribute("totalElements", categories.size());

        return "admin/category";
    }



    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) throws IOException {

        // Validación de nombre
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            session.setAttribute("errorMsg", "Category Name cannot be empty.");
            return "redirect:/admin/category";
        }

        // Asignar imagen (o default)
        String imageName = file.getOriginalFilename();
        if (imageName == null || imageName.isEmpty()) {
            imageName = "default.jpg";
        }
        category.setImageName(imageName);

        // Validar si existe
        Boolean existCategory = categoryService.existCategory(category.getName());

        if (existCategory) {
            session.setAttribute("errorMsg", "Category Name '" + category.getName() + "' already exists.");
        } else {
            Category savedCategory = categoryService.saveCategory(category);

            if (ObjectUtils.isEmpty(savedCategory)) {
                session.setAttribute("errorMsg", "Category could not be saved! Internal server error.");
            } else {
                // Verifica o crea carpeta 'category_img'
                File saveFile = new ClassPathResource("static/img").getFile();
                File categoryImgDir = new File(saveFile, "category_img");

                if (!categoryImgDir.exists()) {
                    categoryImgDir.mkdirs(); // crea carpeta si no existe
                }

                // Guardar archivo
                if (!file.isEmpty()) {
                    Path path = Paths.get(categoryImgDir.getAbsolutePath(), file.getOriginalFilename());
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }

                session.setAttribute("succMsg", "Category saved successfully!");
            }
        }

        return "redirect:/admin/category";
    }


    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable Integer id, Model m) {

        m.addAttribute("category", categoryService.getCategoryById(id));  // Evitar errores en la vista si usas paginación compartida (como en layout base o fragmentos)
        m.addAttribute("pageNo", 0);
        m.addAttribute("isFirst", true);
        m.addAttribute("isLast", true);
        m.addAttribute("totalPages", 1);
        m.addAttribute("totalElements", 1);

        // Evitar errores en la vista si usas paginación compartida (como en layout base o fragmentos)
        return "admin/edit_category";
    }


    // Method to delete category (add this)
    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable Integer id, HttpSession session) {
        Boolean deleteCategory= categoryService.deleteCategory(id);
        if (deleteCategory) {
            session.setAttribute("errorMsg", "Category  deletion!");

        }else {
            session.setAttribute("errorMsg", "Category could not be deleted!");
        }

        return "redirect:/admin/category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
                                 HttpSession session) throws IOException {

        Category oldCategory = categoryService.getCategoryById(category.getId());
        String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

        if (!ObjectUtils.isEmpty(category)) {

            oldCategory.setName(category.getName());
            oldCategory.setIsActive(category.getIsActive());
            oldCategory.setImageName(imageName);
        }

        Category updateCategory = categoryService.saveCategory(oldCategory);

        if (!ObjectUtils.isEmpty(updateCategory)) {

            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
                        + file.getOriginalFilename());

                // System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            session.setAttribute("succMsg", "Category update success");
        } else {
            session.setAttribute("errorMsg", "something wrong on server");
        }

        return "redirect:/admin/loadEditCategory/" + category.getId();
    }
    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("file") MultipartFile image,
                              @RequestParam("category.id") Integer categoryId,
                              HttpSession session) throws IOException {

        // Buscar categoría por ID
        Category category = categoryRepository.findById(categoryId).orElse(null);
        System.out.println(category);
        if (category == null) {
            session.setAttribute("errorMsg", "Invalid category selected.");
            return "redirect:/admin/loadAddProduct";
        }
        product.setCategory(category);

        // Procesar imagen
        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
        product.setImage(imageName);

        // Lógica de precios
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        // Guardar producto
        Product saveProduct = productService.saveProduct(product);

        if (!ObjectUtils.isEmpty(saveProduct)) {
            // Guardar archivo si no está vacío
            if (!image.isEmpty()) {
                File saveDir = new ClassPathResource("static/img/product_img").getFile();
                Path path = Paths.get(saveDir.getAbsolutePath(), imageName);
                Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            session.setAttribute("succMsg", "Product saved successfully");
        } else {
            session.setAttribute("errorMsg", "Something went wrong on the server");
        }

        return "redirect:/admin/loadAddProduct";
    }


    @GetMapping("/products")
    public String viewProducts(Model m,
                               @RequestParam(value = "category", required = false) String category,
                               @RequestParam(value = "active", required = false) String active,
                               @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
                               @RequestParam(defaultValue = "") String ch) {

        Page<Product> page;

        if (StringUtils.hasText(ch)) {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        } else {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        }

        List<Product> products = page.getContent();

        m.addAttribute("products", products);
        m.addAttribute("productsSize", products.size());

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        // Para select de categorías
        m.addAttribute("categories", categoryRepository.findAll());
        m.addAttribute("selectedCategory", category); // útil para el HTML

        return "admin/products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {
        Boolean deleteProduct = productService.deleteProduct(id);
        if (deleteProduct) {
            session.setAttribute("suchMsg", "Product delete success");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id, Model m) {
        m.addAttribute("product", productService.getProductById(id));
        m.addAttribute("categories", categoryService.getAllCategory());
        return "admin/edit_product";
    }




    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
                                HttpSession session, Model m) {

        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "invalid Discount");
        } else {
            Product updateProduct = productService.updateProduct(product, image);
            if (!ObjectUtils.isEmpty(updateProduct)) {
                session.setAttribute("succMsg", "Product update success");
            } else {
                session.setAttribute("errorMsg", "Something wrong on server");
            }
        }
        return "redirect:/admin/editProduct/" + product.getId();
    }


    @GetMapping("/users")
    public String getAllUsers(Model m) {
        List<UserDtls> users = userService.getUsers("ROLE_USER");
        m.addAttribute("users", users);
        return "admin/users";
    }

    public String updateUserAccountStatus(@RequestParam String status, @RequestParam Integer id, HttpSession session) {
        Boolean f = userService.updateAccountStatus(id, Boolean.valueOf(status));
        if(f){
            session.setAttribute("suchMsg", "Account Status");
        }else {
            session.setAttribute("errorMsg", "Something went wrong on the server");
        }

        return "redirect:/admin/users";
    }
}