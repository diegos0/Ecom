package com.ecom.controller;

import com.ecom.model.UserDtls;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static com.ecom.util.CommonUtil.sendMail;

@Controller
public class HomeController {
    @Autowired
    private  CategoryService categoryService;

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserService userService;

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/signin")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable("id") Integer id, Model model) {
        // 1. Obtener el producto por ID
        Product product = productService.getProductById(id);

        // 2. Verificar si el producto existe
        if(product == null) {
            return "redirect:/products"; // Redirige si no existe
        }

        // 3. Agregar el producto al modelo
        model.addAttribute("product", product);

        // 4. Mostrar la vista
        return "view_product";
    }

    @GetMapping("/products")
    public String products(Model m,
                           @RequestParam(value = "category", defaultValue = "") String category,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
                           @RequestParam(defaultValue = "") String ch) {

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramValue", category);
        m.addAttribute("categories", categories);

        Page<Product> page;

        if (StringUtils.isEmpty(ch)) {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        } else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

        List<Product> products = page.getContent();

        m.addAttribute("products", products);
        // ✅ usa la lista paginada
        m.addAttribute("productsSize", products.size()); // esto habilita el <th:block th:if>
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());
        return "product";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user  , @RequestParam("img" )MultipartFile file, HttpSession session) throws IOException {

        // Procesar imagen
        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        user.setProfileImage(imageName);
        UserDtls saveUser = userService.saveUser(user);
        // Lógica de precios

        // Guardar producto

        if (!ObjectUtils.isEmpty(saveUser)) {
            // Guardar archivo si no está vacío
            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            session.setAttribute("suchMsg", "User saved successfully");
        } else {
            session.setAttribute("errorMsg", "Something went wrong on the server user");
        }



        return  "redirect:/register";
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword(){
        return "forgot-password.html";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, HttpSession session, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        UserDtls userByEmail = userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Something wrong on se");





        }else {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email,resetToken);

            // generate URL : http:/localhost:8080/reset-password?token
            String url = CommonUtil.generateUrl(request) +"/reset-password="+resetToken;
            Boolean sendMail = CommonUtil.sendMail(url,email);

            if ( sendMail){
                session.setAttribute("suchMsg", "please check your email Password Reset link sent ");

            }else {
                session.setAttribute("errorMsg", "Something wrong on se");

            }

        }
        return "redirect:/forgot_password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token,HttpSession session,Model m){

        UserDtls userByToken = userService.getUserByResetToken(token);
        if ((userByToken) == null) {
        m.addAttribute("msg", "Something went wrong on se");

        return  "message";
            }
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String ResetPassword(@RequestParam String token,@RequestParam String password, HttpSession session,Model m){

        UserDtls userByToken = userService.getUserByResetToken(token);
        if ((userByToken) == null) {
            m.addAttribute("errorMsg", "Something went wrong on se");

            return  "error";
        }else {
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);
            session.setAttribute("suchMsg", "Password change successfully ");
            m.addAttribute("msg", "Password change successfully ");
            return  "error";
        }
    }
}
