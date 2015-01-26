(ns om-widgets.stylesheet)
(defn- add-style-string!
  [str]
  (let [node (.createElement js/document "style")]
    (set! (.-innerHTML node) str)
    (.appendChild js/document.body node)))

(add-style-string! "

  .om-widgets-overlay {
    position:fixed;
    left:0px;
    top:0px;
    z-index:1000;
    background:gray;
    height:100%;
    width:100%;
    opacity:0.2;
    /* IE 8 */
    -ms-filter: \"progid:DXImageTransform.Microsoft.Alpha(Opacity=20)\";
    /* IE 5-7 */
    filter: alpha(opacity=20);
    /* Netscape */
    -moz-opacity: 0.2;
    /* Safari 1.x */
    -khtml-opacity: 0.2;
    /* Good browsers */
    opacity: 0.2;
  }
  .om-widgets-click-handler {
      position:fixed;
      width:100%;
      height:100%;
      left:0;
      z-index:11000;
      display:block;
      top:0;
  }
  .om-widgets-popover-launcher {
    position: relative;
    display: inline !important;
  }

  .om-widgets-popover {
    position:absolute;
    background:white;
    top:16px;
    z-index:12000;
    padding:15px;
    border: 1px solid rgba(0,0,0,.2);
    border-radius: 6px;
    outline: 0;
    -webkit-box-shadow: 0 3px 9px rgba(0,0,0,.5);
    box-shadow: 0 3px 9px rgba(0,0,0,.5);
		left:0;
  }

  .om-widgets-modal-is-open {
    overflow: hidden;
  }

  .om-widgets-modal-box {
    overflow-x: hidden;
    overflow-y: auto;
    opacity: 1;
    display: block;
    z-index: 10000 !important;
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 1050;
    -webkit-overflow-scrolling: touch;
    outline: 0;
    -webkit-transition: opacity .15s linear;
    -o-transition: opacity .15s linear;
    transition: opacity .15s linear;
  }

  .om-widgets-modal-dialog {
    position: relative;
    width: auto;
    margin: 30px 20px;
  }
  @media (min-width: 768px){
    .om-widgets-modal-dialog {
      position: relative;
      margin: 30px auto;
      width:600px;
    }
  }

  .om-widgets-modal-content {
    position: relative;
    background-color: #fff;
    -webkit-background-clip: padding-box;
    background-clip: padding-box;
    border: 1px solid #999;
    border: 1px solid rgba(0,0,0,.2);
    border-radius: 6px;
    outline: 0;
    -webkit-box-shadow: 0 3px 9px rgba(0,0,0,.5);
    box-shadow: 0 3px 9px rgba(0,0,0,.5);
  }

  .om-widgets-modal-header {
    min-height: 16.43px;
    padding: 15px;
    border-bottom: 1px solid #e5e5e5;
  }
  .om-widgets-modal-title {
    margin: 0;
    line-height: 1.42857143;
    font-size:18px;
    font: arial,sans-serif;
  }
  .om-widgets-modal-body {
    position: relative;
    padding: 15px;
  }
  .om-widgets-modal-body p,.om-widgets-modal-footer p {
    margin: 0 0 10px;
  }
  .om-widgets-modal-footer {
    padding: 15px;
    text-align: right;
    border-top: 1px solid #e5e5e5;
  }





  .om-widgets-checkbox {}
  .om-widgets-checkbox input {
    vertical-align:middle;
  }

  .om-widgets-radio {}
  .om-widgets-radio input {
    top:2px;
    position:relative;
  }

   input.om-widgets-input-text[disabled]{
    background:#aaaaaa !important;
    color:#aaaaaa !important;
   }

   input.om-widgets-input-text[readonly]{
      background:#fcfcfc ;
      color:#808080;

   }

  .om-widgets-combobox-readonly[disabled] {
    background:#fcfcfc !important;
    color:#606060 !important;
  }

  select.om-widgets-combobox[disabled] {
    background:#aaaaaa;
    color:#aaaaaa;

  }

  .om-widgets-grid {
  }
  .om-widgets-grid .table {
    table-layout: fixed !important;
    width: 100%;
    max-width: 100%;
    border-spacing: 0;
    border-collapse: collapse;
  }
  .om-widgets-grid .table>thead>tr>th {
    vertical-align: bottom;
    border-bottom: 2px solid #bababa;
  }

  .om-widgets-default-row {
    cursor:pointer;
  }
  .om-widgets-default-row.success {
    background:#CEE2EC;
  }
  .om-widgets-default-row:hover {
      cursor: pointer;
  }

  .om-widgets-grid table {
    table-layout:fixed ;
  }
  .om-widgets-grid .header {
    overflow: hidden;
    background: rgb(232, 232, 232);
    border-radius: 5px 5px 0 0;
  }
  .om-widgets-grid .pager {
    text-align:center;

  }
  .om-widgets-grid .pager li {
    padding-left:3px;
    cursor:pointer;
    -webkit-user-select: none; /* Chrome/Safari */
    -moz-user-select: none; /* Firefox */
    -ms-user-select: none; /* IE10+ */
    padding-left: 0;
    margin: 20px 0;
    text-align: center;
    list-style: none;
    display:inline;

  }
  .om-widgets-grid .pager li>a {
    display: inline-block;
    padding: 5px 14px;
  }
  .om-widgets-grid .pager li.disabled {
      color: #777;
      cursor: not-allowed;
  }
  .om-widgets-grid .pager .totals {
    padding: 2px 10px;
    float:right;
  }
  .om-widgets-grid .pagination a {
    color:rgb(128, 128, 128);
  }
  .om-widgets-tab {
    width:100%;
    margin:3px;
    margin-bottom:5px;
    overflow:hidden;
  }
  .om-widgets-tab .om-widgets-active-tab {
    display:block;
    width:100%;
    height:100%;
    padding:10px;
/*
    border-bottom: 1px solid #ddd;
    border-left: 1px solid #ddd;
    border-right: 1px solid #ddd;
*/
  }
  .om-widgets-tab .om-widgets-nav-tabs {
     display:table;
     border-bottom: 1px solid #ddd;
     width:100%;
  }
  .om-widgets-tab .om-widgets-right-panel {
    float:right;
    position:relative
  }
  .om-widgets-tab .om-widgets-left-panel {
    float:left;
    position:relative
  }

  .om-widgets-nav-tabs>li.active>a, .om-widgets-nav-tabs>li.active>a:hover, .om-widgets-nav-tabs>li.active>a:focus {
    color: #555;
    cursor: default;
    background-color: #fff;
    border: 1px solid #ddd;
    border-bottom-color: transparent;
  }
  .om-widgets-nav-tabs>li {
    float:left;
    margin-bottom:-1px;
  }
  .om-widgets-nav-tabs>li>a {
    margin-right: 2px;
    line-height: 1.42857143;
    border: 1px solid transparent;
    border-radius: 4px 4px 0 0;
  }
  .om-widgets-tab-item {
    position: relative;
    display: block;
    padding: 10px 15px;
    border-color: #eee #eee #ddd;

  }

/*
  .om-widgets-nav-tabs>li>a:hover {
    border-color: #eee #eee #ddd;
  }
*/

  .om-widgets-tab-item:hover, .om-widgets-tab-item:focus {
    text-decoration: none;
    background-color: #eee;
  }


  .om-widgets-tab .om-widgets-inactive-tab {
    display:none;
  }
  .om-widgets-tab .nav-tabs {
    border-bottom: 1px solid #ddd;
    padding-left: 0;
    margin-bottom: 0;
    list-style: none;
  }

  /* Forms */
   label.om-widgets-control-label {
    display: inline-block;
    max-width: 100%;
    margin-bottom: 5px;
    font-weight: 700;
  }

  .has-error .om-widgets-form-control {
    border-color: #a94442;
    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
  }
  .om-widgets-form-control {
    display: block;
    width: 100%;
    height: 28px;
    padding: 6px 12px;
    font-size: 14px;
    line-height: 18px;
    color: #555;
    background-color: #fff;
    background-image: none;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
  }
  .om-widgets-form-group {
    border: none;
    padding-top: 5px;
  }
  .om-widgets-form-group.required .om-widgets-control-label:not(.om-widgets-radio-label):before {
    content: \"*\";
    margin: 3px;
    vertical-align: middle;
    color: red;
  }

  .om-widgets-form-group.required .om-widgets-checkbox label :before {
    content: \"*\";
    vertical-align: middle;
    margin-left:-10px;
    font-weight: bold;
    font-size:17px;
    color: red;
  }
  .om-widgets-form-group.required .om-widgets-checkbox {
    margin-left: 10px;
  }
  .om-widgets-form-group .om-widgets-checkbox label.om-widgets-label ,.om-widgets-form-group .om-widgets-radio label.om-widgets-label{
    max-width: 100%;
    margin-bottom: 5px;
    font-weight: 700;
  }

  .om-widgets-has-error .om-widgets-help-block {
      color: #a94442;
  }
  .om-widgets-help-block {
    display: block;
    margin-top: 5px;
    margin-bottom: 10px;
    color: #737373;
  }
/* Bootstrap grid system*/
  .om-widgets-container {
    padding-right: 15px;
    padding-left: 15px;
    margin-right: auto;
    margin-left: auto;
  }
  @media (min-width: 768px) {
    .om-widgets-container {
      width: 750px;
    }
  }
  @media (min-width: 992px) {
    .om-widgets-container {
      width: 970px;
    }
  }
  @media (min-width: 1200px) {
    .om-widgets-container {
      width: 1170px;
    }
  }
  .om-widgets-container-fluid {
    padding-right: 15px;
    padding-left: 15px;
    margin-right: auto;
    margin-left: auto;
  }
  .om-widgets-row {
    margin-right: -15px;
    margin-left: -15px;
    margin-bottom:2px;
    margin-top:2px;
    overflow:hidden;
  }

  .om-widgets-col-1, .om-widgets-col-1, .om-widgets-col-2, .om-widgets-col-3, .om-widgets-col-4, .om-widgets-col-5, .om-widgets-col-6, .om-widgets-col-7, .om-widgets-col-8, .om-widgets-col-9, .om-widgets-col-10, .om-widgets-col-11, .om-widgets-col-12 {
    position: relative;
    min-height: 1px;
    padding-right: 15px;
    padding-left: 15px;
    float: left;

  }
  .om-widgets-col-12 {
    width: 100%;
  }
  .om-widgets-col-11 {
    width: 91.66666667%;
  }
  .om-widgets-col-10 {
    width: 83.33333333%;
  }
  .om-widgets-col-9 {
    width: 75%;
  }
  .om-widgets-col-8 {
    width: 66.66666667%;
  }
  .om-widgets-col-7 {
    width: 58.33333333%;
  }
  .om-widgets-col-6 {
    width: 50%;
  }
  .om-widgets-col-5 {
    width: 41.66666667%;
  }
  .om-widgets-col-4 {
    width: 33.33333333%;
  }
  .om-widgets-col-3 {
    width: 25%;
  }
  .om-widgets-col-2 {
    width: 16.66666667%;
  }
  .om-widgets-col-1 {
    width: 8.33333333%;
  }

  .om-widgets-col-pull-12 {
    right: 100%;
  }
  .om-widgets-col-pull-11 {
    right: 91.66666667%;
  }
  .om-widgets-col-pull-10 {
    right: 83.33333333%;
  }
  .om-widgets-col-pull-9 {
    right: 75%;
  }
  .om-widgets-col-pull-8 {
    right: 66.66666667%;
  }
  .om-widgets-col-pull-7 {
    right: 58.33333333%;
  }
  .om-widgets-col-pull-6 {
    right: 50%;
  }
  .om-widgets-col-pull-5 {
    right: 41.66666667%;
  }
  .om-widgets-col-pull-4 {
    right: 33.33333333%;
  }
  .om-widgets-col-pull-3 {
    right: 25%;
  }
  .om-widgets-col-pull-2 {
    right: 16.66666667%;
  }
  .om-widgets-col-pull-1 {
    right: 8.33333333%;
  }
  .om-widgets-col-pull-0 {
    right: auto;
  }
  .om-widgets-col-push-12 {
    left: 100%;
  }
  .om-widgets-col-push-11 {
    left: 91.66666667%;
  }
  .om-widgets-col-push-10 {
    left: 83.33333333%;
  }
  .om-widgets-col-push-9 {
    left: 75%;
  }
  .om-widgets-col-push-8 {
    left: 66.66666667%;
  }
  .om-widgets-col-push-7 {
    left: 58.33333333%;
  }
  .om-widgets-col-push-6 {
    left: 50%;
  }
  .om-widgets-col-push-5 {
    left: 41.66666667%;
  }
  .om-widgets-col-push-4 {
    left: 33.33333333%;
  }
  .om-widgets-col-push-3 {
    left: 25%;
  }
  .om-widgets-col-push-2 {
    left: 16.66666667%;
  }
  .om-widgets-col-push-1 {
    left: 8.33333333%;
  }
  .om-widgets-col-push-0 {
    left: auto;
  }

  .om-widgets-dropdown .dropdown-toggle {
    cursor:pointer;
  }
  .om-widgets-dropdown li>a {
    cursor:pointer;
  }
}
")
