+function(d){var b=function(f,e){this.$element=d(f);this.options=d.extend({},b.DEFAULTS,e);this.init()};b.VERSION="1.0.0";b.DEFAULTS={inputIcon:"fa-calendar",dateLimit:{days:365},autoUpdateInput:true,showDropdowns:true,showWeekNumbers:true,timePicker:false,timePickerIncrement:1,timePicker12Hour:true,buttonClasses:["btn"],applyClass:"green",cancelClass:"default",locale:{format:"YYYY-MM-DD",separator:" ~ ",applyLabel:"确定",fromLabel:"从",toLabel:"到",customRangeLabel:"自由选取",daysOfWeek:["日","一","二","三","四","五","六"],monthNames:["1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"],firstDay:1}};b.prototype.init=function(){var e=this.$element;var g=this.options;if(e.attr("readonly")||e.attr("disabled")){return}e.css({minWidth:"70px"});if(g.dateScope){if(g.dateScope=="afterNow"){g.minDate=moment();g.ranges={"今天":[moment(),moment()],"未来一周":[moment(),moment().add(6,"days")],"未来一月":[moment(),moment().add(29,"days")],"未来一季度":[moment().subtract(89,"days"),moment()],"未来半年":[moment(),moment().add(179,"days")],"未来一年":[moment(),moment().add(364,"days")]}}else{if(g.dateScope=="beforeNow"){g.maxDate=moment();g.ranges={"今天":[moment(),moment()],"昨天":[moment().subtract(1,"days"),moment().subtract(1,"days")],"本月":[moment().startOf("month"),moment().endOf("month")],"上月":[moment().subtract(1,"month").startOf("month"),moment().subtract(1,"month").endOf("month")],"最近一周":[moment().subtract(6,"days"),moment()],"最近一月":[moment().subtract(29,"days"),moment()],"最近一季度":[moment().subtract(89,"days"),moment()],"最近半年":[moment().subtract(179,"days"),moment()],"最近一年":[moment().subtract(364,"days"),moment()]}}}}else{g.ranges={"今天":[moment(),moment()],"昨天":[moment().subtract(1,"days"),moment().subtract(1,"days")],"最近一周":[moment().subtract(6,"days"),moment()],"未来一周":[moment(),moment().add(6,"days")],"最近一月":[moment().subtract(29,"days"),moment()],"未来一月":[moment(),moment().add(29,"days")],"最近一季度":[moment().subtract(89,"days"),moment()],"本月":[moment().startOf("month"),moment().endOf("month")],"上月":[moment().subtract(1,"month").startOf("month"),moment().subtract(1,"month").endOf("month")]}}if(g.inputIcon){e.wrap('<div class="input-icon"></div>');e.before("<i class='fa "+g.inputIcon+"'></i>");e.attr("data-input-icon-done","true")}var f=e.val();e.daterangepicker(g);if(f==""&&(g.dateInit==undefined||g.dateInit===true)){e.val("")}e.on("apply.daterangepicker",function(j,i){e.focus();var h=e.closest("form.form-search-auto");if(h.size()>0){h.submit()}});e.off("focus");e.on("click",function(){if(e.is(":visible")&&d("body").hasClass("modal-open")==false){d("body").addClass("modal-open")}})};function c(e){Util.assert(d.fn.daterangepicker,"依赖组件 daterangepicker 未引入");return this.each(function(){var h=d(this);var g=h.data("extDateRangePicker");var f=typeof e=="object"&&e;if(!g){h.data("extDateRangePicker",(g=new b(this,f)))}if(typeof e=="string"){g[e]()}})}var a=d.fn.extDateRangePicker;d.fn.extDateRangePicker=c;d.fn.extDateRangePicker.Constructor=b;d.fn.extDateRangePicker.noConflict=function(){d.fn.extDateRangePicker=a;return this};Global.addComponent({name:"ExtDateRangePicker",plugin:c,expr:'input[data-picker="date-range"]'})}(jQuery);