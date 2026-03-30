package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.dto.response.CartResponse.CartItemResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       UserRepository userRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) { return mapToResponse(getCartForUser(email)); }

    @Transactional
    public CartResponse addItemToCart(String email, CartItemRequest request) {
        Cart cart = getCartForUser(email);
        Product product = productService.getProductById(request.getProductId());

        if (product.getStockQuantity() < request.getQuantity())
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());

        Optional<CartItem> existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQty)
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, request.getQuantity());
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
        return mapToResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponse updateCartItem(String email, Long productId, CartItemRequest request) {
        Cart cart = getCartForUser(email);
        Product product = productService.getProductById(productId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "productId", productId));
        if (product.getStockQuantity() < request.getQuantity())
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return mapToResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponse removeItemFromCart(String email, Long productId) {
        Cart cart = getCartForUser(email);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "productId", productId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return mapToResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public void clearCart(String email) {
        Cart cart = getCartForUser(email);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    Cart getCartForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private CartResponse mapToResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(i -> new CartItemResponse(i.getId(), i.getProduct().getId(),
                        i.getProduct().getName(), i.getProduct().getPrice(), i.getQuantity()))
                .collect(Collectors.toList());
        return new CartResponse(cart.getId(), items, cart.getTotalPrice());
    }
}
