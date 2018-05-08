+function(d){var c=function(f,e){this.$element=d(f);this.options=d.extend({},c.DEFAULTS,e);this.init()};c.VERSION="1.0.0";c.DEFAULTS={multiple:true,height:"120px",width:"120px",maximumSelectionLength:0,imageHeight:"100%",imageWidth:"100%"};c.prototype.loadKindEditor=function(h,f){var e=this.options;var g=this.kindEditor;if(!g){g=KindEditor.editor({uploadJson:Util.smartParseURL(Config.getFileUploadUrl()),allowFileManager:false,formatUploadUrl:false,afterUpload:h});this.kindEditor=g}if(f||!e.multiple||e.maximumSelectionLength==1){g.loadPlugin("image",function(){g.plugin.imageDialog({showRemote:false,clickFn:function(){}})})}else{g.loadPlugin("multiimage",function(){g.plugin.multiImageDialog({showRemote:false,clickFn:h})})}};c.prototype.init=function(){var n=this.$element;var p=this.options;var j=this;Util.assert(n.is("input")||n.is("select"),"图片上传组件只支持input或select元素类型");n.hide();if(p.imageuploader=="single"){p.multiple=false}else{p.multiple=true}if(n.is("select")){n.addClass("select2-ignore");if(p.multiple){n.attr("multiple","multiple")}}var k=function(q){q.on("load",function(){var s=q.width();var r=q.height();if(s>r){q.css({width:"100%",height:"auto"})}else{q.css({width:"auto",height:"100%"})}})};var e=function(s,r,v){var t=[];var u=Util.smartParseURL(r);t.push('<div class="mt-overlay-1 mt-element-multiimage-item" data-id="'+v+'" data-url="'+r+'" style="width:'+s.width+";height:"+s.height+'; ">');t.push('  <img src="'+u+'"/>');t.push('  <div class="mt-overlay">');t.push('    <ul class="mt-info">');t.push("      <li>");t.push('        <a class="btn default btn-outline btn-remove" href="javascript:;">');t.push('          <i class="fa fa-remove"></i>');t.push("        </a>");t.push("      </li>");t.push("      <li>");t.push('        <a class="btn default btn-outline btn-preview" target="_blank" href="'+u+'">');t.push('          <i class="fa fa-search-plus"></i>');t.push("        </a>");t.push("      </li>");t.push("      <li>");t.push('        <a class="btn default btn-outline btn-edit" href="javascript:;">');t.push('          <i class="fa fa-edit"></i>');t.push("        </a>");t.push("      </li>");t.push("    </ul>");t.push("  </div>");t.push("</div>");var q=d(t.join(""));k(q.children("img"));return q};var o=d('<div class="mt-element-overlay mt-element-multiimage" style="height:auto;min-height:'+p.height+'"></div>');n.after(o);if(n.is("input")){var g=n.val();if(g){var l=g.split(",");d.each(l,function(r,q){o.append(e(p,q,""))})}}else{n.children("option").each(function(){if(d(this).val()){o.append(e(p,d(this).text(),d(this).val()))}})}var h=[];h.push('<div class="mt-overlay-1 mt-element-multiimage-item mt-element-multiimage-add" style="width:'+p.width+";height:"+p.height+'; ">');h.push('  <i class="fa fa-plus" style="line-height: '+p.height+';"></i>');h.push('  <div class="mt-overlay">');h.push('    <ul class="mt-info">');h.push("      <li>");h.push('        <a class="btn default btn-outline btn-add" href="javascript:;">');h.push('          <i class="fa fa-plus"></i>');h.push("        </a>");h.push("      </li>");h.push("    </ul>");h.push("  </div>");h.push("</div>");var i=d(h.join(""));o.append(i);var m=function(){var r=o.children(".mt-element-multiimage-item:not(.mt-element-multiimage-add)");if(n.is("input")){var q=[];r.each(function(){q.push(d(this).attr("data-url"))});n.val(q.join(","))}else{n.children("option").remove();r.each(function(){n.append(new Option(d(this).attr("data-url"),d(this).attr("data-id"),true,true))})}};var f=function(){if(!p.multiple&&o.children(".mt-element-multiimage-item:not(.mt-element-multiimage-add)").size()>0){i.hide()}else{i.show()}m()};f();o.on("click",".btn-add",function(){j.loadKindEditor(function(q,r){j.kindEditor.hideDialog();if(typeof q=="string"){if(Config.isDebugEnable()){console.log("Single image upload:");console.log(r)}i.before(e(p,r.accessUrl,r.id))}else{if(Config.isDebugEnable()){console.log("Multi image upload:");console.log(q)}d.each(q,function(s,t){i.before(e(p,t.accessUrl,t.id))})}f()})});o.on("click",".btn-remove",function(r){var q=d(r.target).closest(".mt-element-multiimage-item");q.remove();f()});o.on("click",".btn-edit",function(r){var q=d(r.target).closest(".mt-element-multiimage-item");j.loadKindEditor(function(s,u){j.kindEditor.hideDialog();q.attr("data-id",u.id);q.attr("data-url",u.accessUrl);var v=Util.smartParseURL(u.accessUrl);var t=q.children("img");t.attr("src",v);k(t);m()},true)})};function b(e){return this.each(function(){var h=d(this);var g=h.data("ExtImageUploader");var f=typeof e=="object"&&e;if(!g){h.data("ExtImageUploader",(g=new c(this,f)))}if(typeof e=="string"){g[e]()}})}var a=d.fn.extImageUploader;d.fn.extImageUploader=b;d.fn.extImageUploader.Constructor=c;d.fn.extImageUploader.noConflict=function(){d.fn.extImageUploader=a;return this};Global.addComponent({name:"ExtImageUploader",plugin:b,runPoint:Global.Component_Run_Point.BeforeAjaxPageShow,expr:"input[data-imageuploader],select[data-imageuploader]",order:900})}(jQuery);