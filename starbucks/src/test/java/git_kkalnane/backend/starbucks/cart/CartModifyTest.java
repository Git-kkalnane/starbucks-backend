package git_kkalnane.backend.starbucks.cart;

import git_kkalnane.backend.starbucks.cart.common.exception.CartErrorCode;
import git_kkalnane.backend.starbucks.cart.common.exception.CartException;
import git_kkalnane.backend.starbucks.cart.domain.Cart;
import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import git_kkalnane.backend.starbucks.cart.dto.request.ModifyCartItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.ModifyCartItemsRequest;
import git_kkalnane.backend.starbucks.cart.dto.response.ModifyCartItemResponse;
import git_kkalnane.backend.starbucks.cart.repository.CartItemRepository;
import git_kkalnane.backend.starbucks.cart.repository.CartRepository;
import git_kkalnane.backend.starbucks.cart.service.CartService;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartModifyTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    private Member member;
    private Cart cart;
    private CartItem cartItem;
    private BeverageItem beverageItem;

    @BeforeEach
    void setup() {
        member = Member.builder().id(1L).build();
        cart = Cart.builder().id(1L).member(member).build();
        beverageItem = BeverageItem.builder().id(10L).price(4500).build();
        cartItem = CartItem.builder()
                .id(100L)
                .cart(cart)
                .beverageItem(beverageItem)
                .cartItemQuantity(1)
                .build();
    }

    @Test
    void 카트_아이템_수정_성공() {
        ModifyCartItemsRequest itemRequest = new ModifyCartItemsRequest(100L, 2, 0);
        ModifyCartItemRequest request = new ModifyCartItemRequest(1L, List.of(itemRequest));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        System.out.println("수정 전 아이템 수량: " + cartItem.getCartItemQuantity());  // 원래 수량
        ModifyCartItemResponse response = cartService.modifiyCartItem(request, 1L);
        System.out.println("수정 후 아이템 수량: " + itemRequest.changeQuantity()); // 요청한 변경 수량

        assertThat(response.totalPrice()).isEqualTo(4500 * 2);

        // 수정된 아이템 ID가 맞는지 확인
        assertThat(response.cartItem()).extracting("id").contains(100L);

        // 수정된 아이템 수량이 요청한 수량과 같은지 확인
        assertThat(response.cartItem()).extracting("quantity").contains(2);
    }


    @Test
    void 존재하지_않는_회원이면_예외발생() {
        ModifyCartItemRequest request = new ModifyCartItemRequest(1L, List.of());

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.modifiyCartItem(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자");
    }

    @Test
    void 존재하지_않는_카트이면_예외발생() {
        ModifyCartItemRequest request = new ModifyCartItemRequest(1L, List.of());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.modifiyCartItem(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 장바구니");
    }

    @Test
    void 존재하지_않는_카트아이템이면_예외발생() {
        ModifyCartItemsRequest itemRequest = new ModifyCartItemsRequest(999L, 2, 0);
        ModifyCartItemRequest request = new ModifyCartItemRequest(1L, List.of(itemRequest));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.modifiyCartItem(request, 1L))
                .isInstanceOf(CartException.class)
                .hasMessageContaining(CartErrorCode.CART_ITEM_NOT_FOUND.getMessage());
    }

    @Test
    void 카트아이템이_사용자의_카트와_매치되지_않으면_예외() {
        Cart otherCart = Cart.builder().id(2L).member(member).build();

        CartItem fakeCartItem = CartItem.builder()
                .id(100L)
                .cart(otherCart)
                .beverageItem(beverageItem)
                .cartItemQuantity(1)
                .build();

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(fakeCartItem));


        ModifyCartItemsRequest itemRequest = new ModifyCartItemsRequest(100L, 2, 0);
        ModifyCartItemRequest request = new ModifyCartItemRequest(1L, List.of(itemRequest));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.modifiyCartItem(request, 1L))
                .isInstanceOf(CartException.class)
                .hasMessageContaining(CartErrorCode.CART_INVALID.getMessage());
    }
}
