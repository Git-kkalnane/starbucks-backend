package git_kkalnane.backend.starbucks.cart;

import git_kkalnane.backend.starbucks.cart.common.exception.CartErrorCode;
import git_kkalnane.backend.starbucks.cart.common.exception.CartException;
import git_kkalnane.backend.starbucks.cart.domain.Cart;
import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import git_kkalnane.backend.starbucks.cart.repository.CartItemRepository;
import git_kkalnane.backend.starbucks.cart.repository.CartRepository;
import git_kkalnane.backend.starbucks.cart.service.CartService;
import git_kkalnane.backend.starbucks.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartDeleteTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    private Cart cart;
    private CartItem cartItem;
    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder().id(1L).build();
        cart = Cart.builder().id(1L).member(member).build();
        cartItem = CartItem.builder().id(10L).cart(cart).build();
    }


    @Test
    void 카트아이템_삭제_성공() {
        when(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(cartItem));

        Long deletedId = cartService.deleteCartItem(10L, 1L);

        verify(cartItemRepository).deleteById(10L);
        assertThat(deletedId).isEqualTo(10L);
    }

    @Test
    void 카트_없으면_예외() {
        when(cartRepository.findByMemberId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteCartItem(10L, 1L))
                .isInstanceOf(CartException.class)
                .hasMessageContaining(CartErrorCode.CART_NOT_FOUND.getMessage());
    }

    @Test
    void 카트아이템_없으면_예외() {
        when(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteCartItem(10L, 1L))
                .isInstanceOf(CartException.class)
                .hasMessageContaining(CartErrorCode.CART_ITEM_NOT_FOUND.getMessage());
    }

    @Test
    void 카트아이템이_사용자의_카트와_다르면_예외() {
        Cart otherCart = Cart.builder().id(2L).build();
        CartItem otherCartItem = CartItem.builder().id(10L).cart(otherCart).build();

        when(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(otherCartItem));

        assertThatThrownBy(() -> cartService.deleteCartItem(10L, 1L))
                .isInstanceOf(CartException.class)
                .hasMessageContaining(CartErrorCode.CART_INVALID.getMessage());
    }

    @Test
    void 멤버와_카트_소유주가_다르면_예외() {

        Cart otherCart = Cart.builder().id(2L).build();
        CartItem otherCartItem = CartItem.builder().id(10L).cart(otherCart).build();

        when(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(otherCartItem));


        assertThatThrownBy(() -> cartService.deleteCartItem(10L, 1L))
                .isInstanceOf(CartException.class)
                .hasMessageContaining(CartErrorCode.CART_INVALID.getMessage());

        verify(cartItemRepository, never()).deleteById(any());
    }

}
