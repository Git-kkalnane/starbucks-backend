package git_kkalnane.backend.starbucks.cart.service;

import git_kkalnane.backend.starbucks.cart.common.exception.CartErrorCode;
import git_kkalnane.backend.starbucks.cart.common.exception.CartException;
import git_kkalnane.backend.starbucks.cart.domain.Cart;
import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import git_kkalnane.backend.starbucks.cart.dto.request.*;
import git_kkalnane.backend.starbucks.cart.dto.response.AddCartItemResponse;
import git_kkalnane.backend.starbucks.cart.dto.response.ModifyCartItemsResponse;
import git_kkalnane.backend.starbucks.cart.dto.response.AddCartItemsResponse;
import git_kkalnane.backend.starbucks.cart.dto.response.ModifyCartItemResponse;
import git_kkalnane.backend.starbucks.cart.repository.CartItemRepository;
import git_kkalnane.backend.starbucks.cart.repository.CartRepository;
import git_kkalnane.backend.starbucks.item.common.exception.ItemErrorCode;
import git_kkalnane.backend.starbucks.item.common.exception.ItemException;
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

import java.util.ArrayList;
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


    /**
     * @param addCartItemRequest : 카트 메뉴 추가 DTO
     * @param memberId           : 회원정보
     * @return : ResponseDTO
     */
    @Transactional
    public AddCartItemResponse addItem(AddCartItemRequest addCartItemRequest, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalStateException("존재하지 않는 사용자입니다."));

        Cart cart = cartRepository.findById(memberId).orElseThrow(
                () -> new CartException(CartErrorCode.CART_NOT_FOUND));

        //TODO : 후에 Store 검증 로직도 필요 시 추가 예정

        List<CartItem> cartItems = addCartItemRequest.items().stream()
                .map(item -> addCartItem(item, cart))
                .collect(Collectors.toList());

        int totalPrice = totalPrice(cartItems);
        cartItemRepository.saveAll(cartItems);

        List<AddCartItemsResponse> cartItemsResponse = CartItemResponse(cartItems);

        return new AddCartItemResponse(
                200,
                "메뉴가 성공적으로 추가되었습니다.",
                cart.getId(),
                cartItemsResponse,
                totalPrice
        );
    }

    private List<AddCartItemsResponse> CartItemResponse(List<CartItem> cartItems) {
        List<AddCartItemsResponse> response = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            String itemName = (cartItem.getBeverageItem() != null)
                    ? cartItem.getBeverageItem().getBeverageItemNameKo()
                    : cartItem.getDessertItem().getDessertItemNameKo();
            int price = (cartItem.getBeverageItem() != null)
                    ? cartItem.getBeverageItem().getPrice()
                    : cartItem.getDessertItem().getPrice();
            response.add(new AddCartItemsResponse(
                    cartItem.getId(),
                    itemName,
                    cartItem.getCartItemQuantity(),
                    price
            ));

        }
        return response;
    }


    /**
     * @param addCartItemOptionRequest : 카트에 담는 아이템 옵션 DTO
     * @return : cartItemOption 반환
     */
    private CartItemOption addCartItemOption(AddCartItemOptionRequest addCartItemOptionRequest) {
        if (addCartItemOptionRequest.itemType() == ItemType.COFFEE) {
            ItemOption itemOption = itemOptionRepository.findById(addCartItemOptionRequest.itemOptionId())
                    .orElseThrow(() -> new ItemException(ItemErrorCode.BEVERAGE_NOT_FOUND));
            return CartItemOption.builder()
                    .itemOption(itemOption)
                    .build();
        } else if (addCartItemOptionRequest.itemType() == ItemType.DESSERT) {
            return CartItemOption.builder()
                    .build();

        } else {
            throw new ItemException(ItemErrorCode.MENU_NOT_FOUND);
        }
    }

    /**
     * @param addItemsRequest : 여러가지 아이템 받을 수 있게 ItemListDTO
     * @param cart         : 어떤 카트에 담는지 알아야하기에 cart 함께 받음
     * @return : cartItem 반환
     */
    private CartItem addCartItem(AddItemsRequest addItemsRequest, Cart cart) {
        List<CartItemOption> cartItemOptions = addItemsRequest.itemOptions().stream()
                .map(this::addCartItemOption)
                .collect(Collectors.toList());

        CartItem cartItem;
        switch (addItemsRequest.itemType()) {
            case COFFEE:
                BeverageItem beverageItem = beverageItemRepository.findById(addItemsRequest.itemId()).orElseThrow(
                        () -> new ItemException(ItemErrorCode.BEVERAGE_NOT_FOUND));
                cartItem = CartItem.builder()
                        .cartItemQuantity(addItemsRequest.quantity())
                        .cart(cart)
                        .beverageItem(beverageItem)
                        .build();
                break;
            case DESSERT:
                DessertItem dessertItem = dessertItemRepository.findById(addItemsRequest.itemId()).orElseThrow(
                        () -> new ItemException(ItemErrorCode.DESSERT_NOT_FOUND));
                cartItem = CartItem.builder()
                        .cartItemQuantity(addItemsRequest.quantity())
                        .cart(cart)
                        .dessertItem(dessertItem)
                        .build();
                break;
            default:
                throw new ItemException(ItemErrorCode.MENU_NOT_FOUND);
        }

        cartItemOptions.forEach(option -> option.setCartItem(cartItem));
        cartItem.setCartItemOption(cartItemOptions);

        return cartItem;

    }

    public int totalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToInt(CartItem::totalPrice)
                .sum();
    }

    /**
     * @param modifyCartItemRequest : 수량 수정 요청 DTO
     * @param memberId              : 멤버ID
     * @return : ResponseDTO
     */
    @Transactional
    public ModifyCartItemResponse modifiyCartItem(ModifyCartItemRequest modifyCartItemRequest, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Cart cart = cartRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 장바구니입니다."));

        List<ModifyCartItemsResponse> updatedCartItems = new ArrayList<>();

        for (ModifyCartItemsRequest cartItems : modifyCartItemRequest.cartItems()) {
            CartItem cartItem = cartItemRepository.findById(cartItems.cartItemId()).orElseThrow(
                    () -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));

            if (!cartItem.getCart().getId().equals(cart.getId())) {
                throw new CartException(CartErrorCode.CART_INVALID);
            }
            cartItem.changeQuantity(cartItems.changeQuantity());

            int unitPrice = (cartItem.getBeverageItem() != null) ?
                    cartItem.getBeverageItem().getPrice() :
                    cartItem.getDessertItem().getPrice();

            int totalPrice = unitPrice * cartItem.getCartItemQuantity();

            updatedCartItems.add(new ModifyCartItemsResponse(
                    cartItem.getId(),
                    cartItems.changeQuantity(),
                    totalPrice
            ));

        }
        int totalPrice = updatedCartItems.stream()
                .mapToInt(ModifyCartItemsResponse::totalPrice)
                .sum();

        return new ModifyCartItemResponse(
                200,
                "아이템 수량이 성공적으로 수정되었습니다.",
                updatedCartItems,
                totalPrice
        );
    }

    /**
     *
     * @param cartItemId : cartItemId 전달받아 cartItemRepository에서 삭제
     * @param memberId : Vaild용
     * @return : 삭제한 cartItemId를 반환한다.
     */
    @Transactional
    public Long deleteCartItem(Long cartItemId, Long memberId) {

        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow(
                () -> new CartException(CartErrorCode.CART_NOT_FOUND));
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartException(CartErrorCode.CART_INVALID);
        }

        cartItemRepository.deleteById(cartItemId);
        return cartItemId;
    }
}







