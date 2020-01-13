import * as $ from "jquery";

declare global {
    export interface JQuery {
        asStopPropagation(): JQuery;

        asPreventDefault(): JQuery;

        asAnchor(): JQuery;

        asComingSoon(): JQuery;
    }
}

export let jqueryExtends = {
    asStopPropagation: function () {
        return this.each((index, element) => {
            $(element).on('click', e => {
                e.stopPropagation();
            });
        });
    },
    asPreventDefault: function () {
        return this.each((index, element) => {
            $(element).on('click', e => {
                e.preventDefault();
            });
        });
    },
    asAnchor: function () {
        return this.each((index, element) => {
            $(element).on('click', e => {
                e.preventDefault();
                window.location = $(this).data('href');
            });
        });
    },
    asComingSoon: function () {
        return this.each((index, element) => {
            $(element).on('click', e => {
                e.preventDefault();
                alert('Coming Soon...')
            });
        });
    }
};