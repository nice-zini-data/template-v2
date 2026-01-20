$(function(){
    searchEvent();
    filterEvent();
});

const searchEvent = () => {
    $('.searchBtn').on('click', () => {
        $('.dashStep02').removeClass('hidden');
        $('.dashStep01').addClass('hidden');
        $('.dashStep02 input').focus();
    });

    $('.backBtn02').on('click', () => {
        $('.dashStep02').addClass('hidden');
        $('.dashStep01').removeClass('hidden');
        $('.dashStep02 input').val('');
        $('.franchiseList').removeClass('hidden');
        $('.franchiseListNull').addClass('hidden');
        $('.clearBtn').addClass('hidden');
    });

    $('.dashStep02 input').on('touchstart keyup', () => {
        const searchValue = $('.dashStep02 input').val();
        if(searchValue !== '' && searchValue.length !== 0){
            $('.franchiseListNull').addClass('hidden');
            $('.franchiseList').removeClass('hidden');
            $('.clearBtn').removeClass('hidden');
        }else{
            $('.franchiseListNull').removeClass('hidden');
            $('.franchiseList').addClass('hidden');
            $('.clearBtn').addClass('hidden');
        }
    });

    $('.clearBtn').on('click', () => {
        $('.dashStep02 input').val('');
        $('.franchiseList').addClass('hidden');
        $('.franchiseListNull').removeClass('hidden');
        $('.clearBtn').addClass('hidden');
    });
}

const filterEvent = () => {
    $('.franchiseListFilterBtn').on('click', () => {
        $('.dashFilterBoxWrap').removeClass('hidden');
        $('.dashFilterBox').removeClass('filterDown')
        setTimeout(() => {
            $('body').addClass('overflow-hidden');
            $('.dashFilterBox').addClass('filterUp');
        }, 100);
    });

    $('.filterBoxContent li').on('pointerup click', function() {
        $('.filterBoxContent li').removeClass('active');
        $(this).addClass('active');
        $('.franchiseListFilterBtn span').text($(this).text());
    });

    $('.filterClose, .filterBoxContent li').on('click', function() {
        $('body').removeClass('overflow-hidden');
        $('.dashFilterBox').removeClass('filterUp').addClass('filterDown');
        setTimeout(() => {
            $('.dashFilterBoxWrap').addClass('hidden');
        }, 300);
    });
}