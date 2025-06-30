package git_kkalnane.backend.starbucks.cart;

import git_kkalnane.backend.starbucks.cart.common.exception.CartException;
import git_kkalnane.backend.starbucks.cart.domain.Cart;
import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import git_kkalnane.backend.starbucks.cart.dto.request.AddCartItemOptionRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.AddCartItemRequest;
import git_kkalnane.backend.starbucks.cart.dto.request.AddItemsRequest;
import git_kkalnane.backend.starbucks.cart.dto.response.AddCartItemResponse;
import git_kkalnane.backend.starbucks.cart.repository.CartItemRepository;
import git_kkalnane.backend.starbucks.cart.repository.CartRepository;
import git_kkalnane.backend.starbucks.cart.service.CartService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartCreateTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ItemOptionRepository itemOptionRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private BeverageItemRepository beverageItemRepository;
    @Mock
    private DessertItemRepository dessertItemRepository;

    private Member member;
    private Cart cart;
    private BeverageItem beverageItem;
    private ItemOption itemOption;

    /**
     * 테스트마다 공통으로 사용할 기본값 세팅
     */
    @BeforeEach
    void setup() {
        member = Member.builder().id(1L).build();
        cart = Cart.builder().id(1L).member(member).build();
        beverageItem = BeverageItem.builder().id(10L).price(5000).beverageItemNameKo("아이스 아메리카노").build();
        itemOption = ItemOption.builder().id(100L).build();
    }


    @Test
    void 카트_아이템_추가_성공() {
        AddCartItemOptionRequest optionRequest = new AddCartItemOptionRequest(
                100L, "바닐라", true, 1, 500, ItemType.BEVERAGE
        );
        AddItemsRequest itemsRequest = new AddItemsRequest(
                10L, ItemType.BEVERAGE, 2, "", null, null, List.of(optionRequest)
        );
        AddCartItemRequest request = new AddCartItemRequest(
                1L, List.of(itemsRequest), 10000
        );

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(member.getId())).thenReturn(Optional.of(cart));
        when(beverageItemRepository.findById(10L)).thenReturn(Optional.of(beverageItem));
        when(itemOptionRepository.findById(100L)).thenReturn(Optional.of(itemOption));

        AddCartItemResponse response = cartService.addItem(request, 1L);

        assertThat(response.cartItem()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(10000);

        verify(cartItemRepository).saveAll(any());
    }

    @Test
    void 카트아이템_디저트_추가성공() {
        Long memberId = 2L;
        Long dessertItemId = 202L;

        Member member = Member.builder()
                .id(memberId)
                .build();

        Cart cart = Cart.builder().id(memberId).member(member).build();
        DessertItem dessertItem = DessertItem.builder()
                .id(dessertItemId)
                .dessertItemNameKo("치즈케이크")
                .price(5500)
                .build();

        AddItemsRequest itemRequest = new AddItemsRequest(
                dessertItemId,
                ItemType.DESSERT,
                1,
                null,
                null,
                null,
                List.of() // 디저트는 옵션 없음
        );

        AddCartItemRequest request = new AddCartItemRequest(null, List.of(itemRequest), 0);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(member.getId())).thenReturn(Optional.of(cart));
        when(dessertItemRepository.findById(dessertItemId)).thenReturn(Optional.of(dessertItem));

        AddCartItemResponse response = cartService.addItem(request, memberId);

        assertThat(response.cartItem().get(0).itemName()).isEqualTo("치즈케이크");
    }
    @Test
    void 카트아이템_디저트음료수_추가성공() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().id(memberId).build();
        Cart cart = Cart.builder().id(memberId).member(member).build();

        BeverageItem beverageItem = BeverageItem.builder()
                .id(100L)
                .beverageItemNameKo("카페라떼")
                .price(4500)
                .build();

        DessertItem dessertItem = DessertItem.builder()
                .id(200L)
                .dessertItemNameKo("치즈케이크")
                .price(5000)
                .build();

        ItemOption option = ItemOption.builder()
                .id(300L)
                .additionalPrice(500)
                .build();

        AddCartItemOptionRequest beverageOptionRequest= new AddCartItemOptionRequest(
                300L, "바닐라 시럽", true, 1, 500, ItemType.BEVERAGE
        );

        AddItemsRequest beverageRequest = new AddItemsRequest(
                100L,
                ItemType.BEVERAGE,
                1,
                null,
                null,
                null,
                List.of(beverageOptionRequest)
        );

        AddItemsRequest dessertRequest = new AddItemsRequest(
                200L,
                ItemType.DESSERT,
                2,
                null,
                null,
                null,
                List.of()
        );

        AddCartItemRequest request = new AddCartItemRequest(
                1L,
                List.of(beverageRequest, dessertRequest),
                15000
        );

        // mocking
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(dessertItemRepository.findById(200L)).willReturn(Optional.of(dessertItem));
        given(itemOptionRepository.findById(300L)).willReturn(Optional.of(option));

        // when
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(beverageItemRepository.findById(100L)).thenReturn(Optional.of(beverageItem));  // 반드시 필요

        AddCartItemResponse response = cartService.addItem(request, memberId);

        // then

        assertThat(response.cartItem()).hasSize(2);
        assertThat(response.cartItem().get(0).itemName()).contains("카페라떼");
        assertThat(response.cartItem().get(1).itemName()).contains("치즈케이크");
        assertThat(response.totalPrice()).isEqualTo(15000);
    }
    @Test
    void 존재하지_않는_회원이면_예외발생() {
        // given
        Long memberId = 99L;
        AddCartItemRequest request = new AddCartItemRequest(memberId, List.of(), 0);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> cartService.addItem(request, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    void 존재하지_않는_카트이면_예외발생() {
        // given
        Long memberId = 1L;
        AddCartItemRequest request = new AddCartItemRequest(memberId, List.of(), 0);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());


        // then
        assertThatThrownBy(() -> cartService.addItem(request, memberId))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("카트가 존재하지 않습니다.");
    }


    @Test
    void 존재하지_않는_음료이면_예외발생() {
        AddItemsRequest itemsRequest = new AddItemsRequest(
                999L, ItemType.BEVERAGE, 1, null, null, null, List.of()
        );
        AddCartItemRequest request = new AddCartItemRequest(1L, List.of(itemsRequest), 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart));
        when(beverageItemRepository.findById(999L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> cartService.addItem(request, 1L))
                .isInstanceOf(ItemException.class)
                .hasMessageContaining("존재하지 않는 음료입니다.");
    }

    @Test
    void 존재하지_않는_디저트이면_예외발생() {
        AddItemsRequest itemsRequest = new AddItemsRequest(
                999L, ItemType.DESSERT, 1, null, null, null, List.of()
        );
        AddCartItemRequest request = new AddCartItemRequest(1L, List.of(itemsRequest), 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(member.getId())).thenReturn(Optional.of(cart));
        when(dessertItemRepository.findById(999L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> cartService.addItem(request, 1L))
                .isInstanceOf(ItemException.class)
                .hasMessageContaining("존재하지 않는 디저트입니다.");
    }

    @Test
    void 존재하지_않는_옵션이면_예외발생() {
        AddCartItemOptionRequest optionRequest = new AddCartItemOptionRequest(
                999L, "허니시럽", true, 1, 500, ItemType.BEVERAGE
        );
        AddItemsRequest itemsRequest = new AddItemsRequest(
                10L, ItemType.BEVERAGE, 1, null, null, null, List.of(optionRequest)
        );
        AddCartItemRequest request = new AddCartItemRequest(1L, List.of(itemsRequest), 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(member.getId())).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(999L)).thenReturn(Optional.empty());


        // then
        assertThatThrownBy(() -> cartService.addItem(request, 1L))
                .isInstanceOf(ItemException.class)
                .satisfies(ex -> {
                    ItemException ie = (ItemException) ex;
                    assertThat(ie.getErrorCode()).isEqualTo(ItemErrorCode.BEVERAGE_NOT_FOUND);
                });
    }

    @Test
    void 카트아이템_가격계산_정상작동() {
        BeverageItem item = BeverageItem.builder().price(4500).build();
        ItemOption option = ItemOption.builder().additionalPrice(500).build();
        CartItemOption cartItemOption = CartItemOption.builder().itemOption(option).build();
        CartItem cartItem = CartItem.builder()
                .beverageItem(item)
                .cartItemQuantity(2)
                .cartItemOption(List.of(cartItemOption))
                .build();

        int total = cartItem.totalPrice();

        assertThat(total).isEqualTo((4500 + 500) * 2);
    }

}





