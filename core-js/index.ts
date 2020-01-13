import * as $ from 'jquery'
import * as _s from 'underscore.string'
import {jqueryExtends} from './src/as'

$(function () {
    $.fn.extend(jqueryExtends);
    Object.keys($.fn)
        .filter(value => value.length > 2 && value.startsWith("as"))
        .forEach(value => $(`.-${_s.dasherize(value)}`)[value]());
});

window['$'] = $;
// @ts-ignore
window['_s'] = _s;