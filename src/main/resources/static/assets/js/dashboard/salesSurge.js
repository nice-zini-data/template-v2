$(function(){
    tabClick();
});

const tabClick = () => {
    $('.dashSubTopTab li').on('click', function(){
        $('.dashSubTopTab li').removeClass('active');
        $(this).addClass('active');
        var idx = $('.dashSubTopTab li').index(this);
        $('.dashSubContentInner').removeClass('active');
        $('.dashSubContentInner').eq(idx).addClass('active');
    });
};