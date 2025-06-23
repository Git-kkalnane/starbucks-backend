package git_kkalnane.backend.starbucks.cart.service;

import git_kkalnane.backend.starbucks.cart.common.exception.CartErrorCode;
import git_kkalnane.backend.starbucks.cart.common.exception.CartException;
import git_kkalnane.backend.starbucks.cart.domain.Cart;
import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import git_kkalnane.backend.starbucks.cart.dto.request.CartAddItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.CartItemOptionRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.ItemsRequest;
import git_kkalnane.backend.starbucks.cart.dto.response.CartAddItemResponse;
import git_kkalnane.backend.starbucks.cart.repository.CartItemRepository;
import git_kkalnane.backend.starbucks.cart.repository.CartRepository;
import git_kkalnane.backend.starbucks.item.domain.ItemOption;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.CartItemOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import git_kkalnane.backend.starbucks.item.repository.ItemOptionRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final CartItemRepository cartItemRepository;
    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;


    @Transactional
    public CartAddItemResponse AddItem(CartAddItemRequest cartAddItemRequest, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalStateException("존재하지 않는 사용자입니다."));

        Cart cart = cartRepository.findById(memberId).orElseThrow(
                () -> new CartException(CartErrorCode.CART_NOT_FOUND));

        //TODO : 후에 Store 검증 로직도 필요 시 추가 예정

        List<CartItem> cartItems = cartAddItemRequest.items().stream()
                        .map(item -> addCartItem(item, cart))
                                .collect(Collectors.toList());

        cartItemRepository.saveAll(cartItems);

        return new CartAddItemResponse(
                200,
                "메뉴가 성공적으로 추가되었습니다.",
                cart.getId(),
                cartItems
        );
    }

    private CartItemOption addCartItemOption(CartItemOptionRequest cartItemOptionRequest) {
        if(cartItemOptionRequest.itemType() ==ItemType.DRINK) {
            ItemOption itemOption = itemOptionRepository.findById(cartItemOptionRequest.itemOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다."));
            return CartItemOption.builder()
                    .itemOption(itemOption)
                    .build();
        }else if(cartItemOptionRequest.itemType() == ItemType.DESSERT) {
           return CartItemOption.builder()
                   .build();

        } else {
            throw new IllegalArgumentException("메뉴 타입이 잘못됐습니다.");
        }
    }

    private CartItem addCartItem(ItemsRequest itemsRequest, Cart cart) {
        List<CartItemOption> cartItemOptions = itemsRequest.itemOptions().stream()
                .map(this :: addCartItemOption)
                .collect(Collectors.toList());

        CartItem cartItem;
        switch (itemsRequest.itemType()) {
            case DRINK:
                BeverageItem beverageItem = beverageItemRepository.findById(itemsRequest.itemId()).orElseThrow(
                        () -> new IllegalArgumentException("존재하지 않는 음료입니다."));
                cartItem = CartItem.builder()
                        .cartItemQuantity(itemsRequest.quantity())
                        .cart(cart)
                        .beverageItem(beverageItem)
                        .build();
                break;
                case DESSERT:
                    DessertItem dessertItem = dessertItemRepository.findById(itemsRequest.itemId()).orElseThrow(
                            ()-> new IllegalArgumentException("존재하지 않는 디저트입니다."));
                    cartItem = CartItem.builder()
                            .cartItemQuantity(itemsRequest.quantity())
                            .cart(cart)
                            .dessertItem(dessertItem)
                            .build();
                    break;
                    default:
                        throw new IllegalArgumentException("잘못된 메뉴입니다.");
        }

        cartItemOptions.forEach(option -> option.setCartItem(cartItem));
        cartItem.setCartItemOption(cartItemOptions);

        return cartItem;

    }

}





