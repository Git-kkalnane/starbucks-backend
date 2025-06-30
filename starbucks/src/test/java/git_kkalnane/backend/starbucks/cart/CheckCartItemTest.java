package git_kkalnane.backend.starbucks.cart;

import git_kkalnane.backend.starbucks.cart.common.exception.CartException;
import git_kkalnane.backend.starbucks.cart.domain.Cart;
import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import git_kkalnane.backend.starbucks.cart.dto.request.CheckCartItemOptionRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.CheckCartItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.response.CheckCartItemResponse;
import git_kkalnane.backend.starbucks.cart.repository.CartItemRepository;
import git_kkalnane.backend.starbucks.cart.repository.CartRepository;
import git_kkalnane.backend.starbucks.cart.service.CartService;
import git_kkalnane.backend.starbucks.item.domain.ItemOption;
import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.CartItemOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CheckCartItemTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Test
    void 장바구니_조회_성공() {
        // given
        Long memberId = 1L;
        Cart mockCart = Cart.builder().id(1L).build();

        BeverageItem beverageItem = BeverageItem.builder()
                .id(10L)
                .beverageItemNameKo("아메리카노")
                .price(4500)
                .build();

        ItemOption option1 = ItemOption.builder()
                .id(100L)
                .syrupName("바닐라")
                .quantity(1)
                .additionalPrice(500)
                .build();

        CartItemOption cartItemOption = CartItemOption.builder()
                .itemOption(option1)
                .build();

        CartItem cartItem = CartItem.builder()
                .id(1000L)
                .cartItemQuantity(2)
                .beverageItem(beverageItem)
                .cartItemOption(List.of(cartItemOption))
                .build();

        // stub
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(mockCart));
        given(cartItemRepository.findAllByCartId(mockCart.getId())).willReturn(List.of(cartItem));

        // when
        CheckCartItemResponse response = cartService.getCartItems(memberId);

        // then
        assertThat(response.cartItem()).hasSize(1);

        CheckCartItemRequest resultItem = response.cartItem().get(0);
        assertThat(resultItem.itemType()).isEqualTo(ItemType.BEVERAGE);
        assertThat(resultItem.itemName()).isEqualTo("아메리카노");
        assertThat(resultItem.quantity()).isEqualTo(2);
        assertThat(resultItem.options()).hasSize(1);
        assertThat(resultItem.options().get(0).syrupName()).isEqualTo("바닐라");
    }

    @Test
    void 장바구니가_존재하지_않을경우_예외발생() {
        // given
        Long memberId = 999L;
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.getCartItems(memberId))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("카트가 존재하지 않습니다.");
    }

    @Test
    void 장바구니에_음료와_디저트가_모두_포함되었을때조회() {
        // given
        Long memberId = 1L;
        Cart mockCart = Cart.builder().id(1L).build();

        // 음료 항목
        BeverageItem americano = BeverageItem.builder()
                .id(10L)
                .beverageItemNameKo("아메리카노")
                .price(4500)
                .build();

        ItemOption vanillaOption = ItemOption.builder()
                .id(100L)
                .syrupName("바닐라")
                .quantity(1)
                .additionalPrice(500)
                .build();

        CartItemOption beverageOption = CartItemOption.builder()
                .itemOption(vanillaOption)
                .build();

        CartItem beverageCartItem = CartItem.builder()
                .id(1000L)
                .cart(mockCart)
                .cartItemQuantity(2)
                .beverageItem(americano)
                .cartItemOption(List.of(beverageOption))
                .build();

        // 디저트 항목
        DessertItem cheesecake = DessertItem.builder()
                .id(20L)
                .dessertItemNameKo("치즈케이크")
                .price(6000)
                .build();

        CartItem dessertCartItem = CartItem.builder()
                .id(2000L)
                .cart(mockCart)
                .cartItemQuantity(1)
                .dessertItem(cheesecake)
                .cartItemOption(List.of()) // 옵션 없음
                .build();

        // stub
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(mockCart));
        given(cartItemRepository.findAllByCartId(mockCart.getId()))
                .willReturn(List.of(beverageCartItem, dessertCartItem));

        // when
        CheckCartItemResponse response = cartService.getCartItems(memberId);

        // then
        assertThat(response.cartItem()).hasSize(2);

        int totalPrice = 0;
        for (CheckCartItemRequest item : response.cartItem()) {
            System.out.println("===== 장바구니 항목 조회 결과 =====");
            System.out.println("ID: " + item.cartItemId());
            System.out.println("이름: " + item.itemName());
            System.out.println("타입: " + item.itemType());
            System.out.println("수량: " + item.quantity());
            System.out.println("옵션 개수: " + item.options().size());

            int itemBasePrice = 0;
            if (item.itemType() == ItemType.BEVERAGE) {
                assertThat(item.itemName()).isEqualTo("아메리카노");
                itemBasePrice = 4500;
            } else if (item.itemType() == ItemType.DESSERT) {
                assertThat(item.itemName()).isEqualTo("치즈케이크");
                itemBasePrice = 6000;
            }

            int optionTotal = item.options().stream()
                    .mapToInt(CheckCartItemOptionRequest::additionalPrice)
                    .sum();

            int itemTotalPrice = (itemBasePrice + optionTotal) * item.quantity();
            totalPrice += itemTotalPrice;

            for (CheckCartItemOptionRequest option : item.options()) {
                System.out.println("  - 시럽: " + option.syrupName());
                System.out.println("    수량: " + option.quantity());
                System.out.println("    추가금액: " + option.additionalPrice());
            }

            System.out.println("▶ 항목별 총액: " + itemTotalPrice + "원");
        }

        System.out.println("🧾 장바구니 총합: " + totalPrice + "원");

        // 최종 총합 검증 (아메리카노 4500 + 500 = 5000 * 2 + 치즈케이크 6000 = 16000원)
        assertThat(totalPrice).isEqualTo(16000);
    }


}
