import * as $ from "jquery";

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
    }
};