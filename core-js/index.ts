import * as $ from 'jquery'

class Student {
    fullName: string;
    constructor(public firstName: string, public middleInitial: string, public lastName: string) {
        this.fullName = firstName + " " + middleInitial + " " + lastName;
    }
}

interface Person {
    firstName: string;
    lastName: string;
}

function greeter(person: Person) {
    return "Hello, " + person.firstName + " " + person.lastName;
}

$(function () {
    let user = new Student("Jane", "M.", "User");
    document.body.textContent = greeter(user);

    $.fn.extend({
        asStopPropagation: function () {
            return this.each(() => {
                $(this).on('click', e => {
                    e.stopPropagation();
                });
            });
        }
    });

    console.log($.fn);
});