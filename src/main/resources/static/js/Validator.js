// let x = document.querySelector('form-massage');
// x.parentElement

// đối tượng `Validator`
function Validator(options) {
    // lấy thẻ cha để chọc xuống thẻ massage lỗi 
    function getParent(element, selector) {
        while(element.parentElement) {
            if (element.parentElement.matches(selector)) {
				
                return element.parentElement;
            }
            element = element.parentElement;
        }
    }

    var selectorRules = {};
    // hàm thực hiện validate
    function validate(inputElement, rule) {
        var errorElement = getParent(inputElement, options.formGroupSelector).querySelector(options.errorSelector);
        var errorMessage;

        var rules = selectorRules[rule.selector];

        for (var i = 0; i < rules.length; i++) {
            switch (inputElement.type) {
                case 'checkbox':
                case 'radio':
                    errorMessage = rules[i](
                        formElement.querySelector(rule.selector + ':checked')
                    );
                    break;
                default:
                    errorMessage = rules[i](inputElement.value);
            }
            if (errorMessage) break;
        }

        if (errorMessage) {
            errorElement.innerText = errorMessage;
            getParent(inputElement, options.formGroupSelector).classList.add('invalid');
        } else {
            errorElement.innerText = '';
            getParent(inputElement, options.formGroupSelector).classList.remove('invalid');

        }
        return !errorMessage;
    }

    // lấy element của form cần validate
    var formElement = document.querySelector(options.form);
    if (formElement) {

        // xử lí các sự kiện 
        // khi submit form
        formElement.onsubmit = function (e) {
            e.preventDefault();

            var isFormValid = true;
            // lặp qua các rule và validate luôn
            options.rules.forEach(function (rule) {
                
                var inputElement = formElement.querySelector(rule.selector);
                var isValid = validate(inputElement, rule);
                if (!isValid) {
                    isFormValid = false;
                }
            });
            // xử lí submit
            if (isFormValid) {
                if (typeof options.onSubmit === 'function') {
	
                    var enableInputs = formElement.querySelectorAll('[name]');
                    var formValue = Array.from(enableInputs).reduce(function (values, input) {
                        switch (input.type){
                            case 'radio':
                                values[input.name] = formElement.querySelector('input[name="'+input.name+'"]:checked').value;
                                break;
                            case 'checkbox':
                                values[input.name] = formElement.querySelector('input[name="'+input.name+'"]:checked').value;
                                if(!input.matches(':checked')) return values;
                                if(!Array.isArray(values[input.name])){
                                    values[input.name] = [];
                                }
                                values[input.name].push(input.value);
                            break;
                            default: 
                            values[input.name] = input.value
                        }
                        
                        return values;
                    }, {});
                    
                } else {
                    // submit theo html
                    formElement.submit();
                    
                }
            }

        }
        options.rules.forEach(function (rule) {
            // lưu lại các rule khi lặp qua
            if (Array.isArray(selectorRules[rule.selector])) {
                selectorRules[rule.selector].push(rule.test);
            } else {
                selectorRules[rule.selector] = [rule.test];
            }
            var inputElement = formElement.querySelector(rule.selector);
            // var errorElement = getParent(inputElement, options.formGroupSelector).querySelector(options.errorSelector);

            if (inputElement) {
                inputElement.onblur = function () {
                    validate(inputElement, rule);
                }
                // xử lý mỗi khi người dung nhập
                inputElement.oninput = function () {
                    var errorElement = getParent(inputElement, options.formGroupSelector).querySelector('.form-message');
                    // báo lỗi trên giao diện
                    errorElement.innerText = '';
                    getParent(inputElement, options.formGroupSelector).classList.remove('invalid');
                }
            }

        });
    }
}
//định nghĩa rules
Validator.isResquired = function (selector, message) {
    return {
        selector: selector,
        test: function (value) {
           
            return value ? undefined : message ||  `Vui lòng nhập trường này` ;
        }
    };
}
Validator.isEmail = function (selector) {
    return {
        selector: selector,
        test: function (value) {
            var regex = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
            return regex.test(value) ? undefined : `Email không hợp lệ`;
        }
    };
}
Validator.isPhoneNumber = function (selector) {
    return {
        selector: selector,
        test: function (value) {
            var regex =  /(03|05|07|08|09|01[2|6|8|9])+([0-9]{8})\b/;
            return regex.test(value) ? undefined : `Số điện thoại không hợp lệ`;
        }
    };
}

Validator.isNumber = function (selector) {
    return {
        selector: selector,
        test: function (value) {
            var regex =  /^[1-9]\d*(,\d+)?$/;
            return regex.test(value) ? undefined : `Trường này phải là ký tự số`;
        }
    };
}
Validator.isNumberReal = function (selector) {
    return { 
    	selector: selector,
        test: function (value) {
            var regex =  /^[0-9]+.[0-9]+$/;
            return regex.test(value) ? undefined : `Trường này phải là ký số`;
        	}
        }
}

Validator.minLength = function (selector, min) {
    return {
        selector: selector,
        test: function (value) {
            return value.length >= min ? undefined : `Vui lòng nhập tối thiểu ${min} kí tự`;
        }
    };
}
Validator.isConfirmed = function (selector, getConfirmValue, message) {
    return {
        selector: selector,
        test: function (value) {
            return value === getConfirmValue() ? undefined : message || 'Giá trị nhập lại không chính xác';
        }
    }
}
