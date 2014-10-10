(ns intception-widgets.stylesheet)
(defn- add-style-string!
  [str]
  (let [node (.createElement js/document "style")]
    (set! (.-innerHTML node) str)
    (.appendChild js/document.body node)))

(add-style-string! "

  .overlay {
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


  .modal-is-open {
    overflow: hidden;
  }

  .modal-box {
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

  .modal-dialog {
    position: relative;
    width: auto;
    margin: 30px 20px;
  }
  @media (min-width: 768px){
    .modal-dialog {
      position: relative;
      margin: 30px auto;
      width:600px;
    }
  }

  .modal-content {
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

  .modal-header {
    min-height: 16.43px;
    padding: 15px;
    border-bottom: 1px solid #e5e5e5;
  }
  .modal-title {
    margin: 0;
    line-height: 1.42857143;
    font-size:18px;
    font: arial,sans-serif;
  }
  .modal-body {
    position: relative;
    padding: 15px;
  }
  .modal-body p,.modal-footer p {
    margin: 0 0 10px;
  }
  .modal-footer {
    padding: 15px;
    text-align: right;
    border-top: 1px solid #e5e5e5;
  }





  .checkbox {
    font: small arial,sans-serif;
    font-size:12px;
    color: #404040;
    line-height:20px;
  }
  .checkbox input {
    vertical-align:middle;
  }

  .radio {
    font: small arial,sans-serif;
    font-size:12px;

    color: #404040;
    line-height:20px;
  }
  .radio input {
    top:2px;
    position:relative;
  }

  .input-text,.combobox {
    font: small arial,sans-serif;
    font-size:12px;
    background:white;
    color: #404040;
    border: 1px solid gray;
    border-radius: 5px;
    box-shadow: 3px 3px 2px #dddddd;
    line-height:20px;
    padding: 2px;
    -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;

   }
   input.input-text[disabled]{
    background:#aaaaaa !important;
    color:#aaaaaa !important;
   }

   input.input-text[readonly]{
      background:#fcfcfc ;
      color:#808080;

   }

  .combobox-readonly[disabled] {
    background:#fcfcfc !important;
    color:#606060 !important;

  }
  select.combobox[disabled] {
    background:#aaaaaa;
    color:#aaaaaa;

  }


  .grid .dummy-col {
    background:white !important;
    color:white !important;
    border:none;
    cursor: default;
    border-top: 1px solid white !important;
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    -khtml-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }
  .grid {
    border: 1px solid gray;
    border-radius: 5px;
    box-shadow: 3px 3px 2px #dddddd;
    color: #404040;
    font: small arial,sans-serif;
    font-size: 12px;
    text-align: center;
    background: white;
    min-width: 330px;

  }
  .grid .table {
    table-layout: fixed !important;
    width: 100%;
    max-width: 100%;
    border-spacing: 0;
    border-collapse: collapse;
  }
  .grid .table>thead>tr>th {
    vertical-align: bottom;
    border-bottom: 2px solid #bababa;
  }

  .grid .scrollable {
    overflow-y:auto;
  }
  .grid .scrollable thead {
    display:block;
    margin-top:-25px;

  }

  .grid td,th{
    text-align:left;
    padding:4px;
    font-size:12px;
    cursor:pointer;
  }
  .grid tr.success td {
    background:#CEE2EC;
  }

  .grid th:hover {
      cursor: pointer;
  }

  .grid table {
    table-layout:fixed ;
  }
  .grid .header {
    overflow: hidden;
    background: rgb(232, 232, 232);
    border-radius: 5px 5px 0 0;
  }
  .grid .pager {
    text-align:center;

  }
  .grid .pager li {
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
  .grid .pager li>a {
    display: inline-block;
    padding: 5px 14px;
    background-color: #fff;
    border: 1px solid #ddd;
    border-radius: 8px;
    width:70px;
    margin:3px;
  }
  .grid .pager li.disabled {
      color: #777;
      cursor: not-allowed;
      background-color: #fff;
  }
  .grid .pager .totals {
    margin:3px;
    border-radius:4px;
    width:50px;
    border: 1px solid #ddd;
    padding: 2px 10px;
    float:right;
  }
  .grid .pagination a {
    color:rgb(128, 128, 128);
  }

  /* Forms */
   label.control-label {
    display: inline-block;
    max-width: 100%;
    margin-bottom: 5px;
    font-weight: 700;
  }

  .has-error .form-control {
    border-color: #a94442;
    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
  }
  .form-control {
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
  .form-group {
    border: none;
    padding-top: 5px;
  }
  .form-group.required .control-label:not(.radio-label):before {
    content: \"*\";
    margin: 3px;
    vertical-align: middle;
    color: red;
  }

  .form-group.required .checkbox label :before {
    content: \"*\";
    vertical-align: middle;
    margin-left:-10px;
    font-weight: bold;
    font-size:17px;
    color: red;
  }
  .form-group.required .checkbox {
    margin-left: 10px;
  }
  .form-group .checkbox label.label ,.form-group .radio label.label{
    max-width: 100%;
    margin-bottom: 5px;
    font-weight: 700;
  }

  .has-error .help-block {
      color: #a94442;
  }
  .help-block {
    display: block;
    margin-top: 5px;
    margin-bottom: 10px;
    color: #737373;
  }
/* Bootstrap grid system*/
  .container {
    padding-right: 15px;
    padding-left: 15px;
    margin-right: auto;
    margin-left: auto;
  }
  @media (min-width: 768px) {
    .container {
      width: 750px;
    }
  }
  @media (min-width: 992px) {
    .container {
      width: 970px;
    }
  }
  @media (min-width: 1200px) {
    .container {
      width: 1170px;
    }
  }
  .container-fluid {
    padding-right: 15px;
    padding-left: 15px;
    margin-right: auto;
    margin-left: auto;
  }
  .row {
    margin-right: -15px;
    margin-left: -15px;
    margin-bottom:2px;
    margin-top:2px;

    overflow:hidden;
  }

  .col-1, .col-1, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-10, .col-11, .col-12 {
    position: relative;
    min-height: 1px;
    padding-right: 15px;
    padding-left: 15px;
    float: left;

  }
  .col-12 {
    width: 100%;
  }
  .col-11 {
    width: 91.66666667%;
  }
  .col-10 {
    width: 83.33333333%;
  }
  .col-9 {
    width: 75%;
  }
  .col-8 {
    width: 66.66666667%;
  }
  .col-7 {
    width: 58.33333333%;
  }
  .col-6 {
    width: 50%;
  }
  .col-5 {
    width: 41.66666667%;
  }
  .col-4 {
    width: 33.33333333%;
  }
  .col-3 {
    width: 25%;
  }
  .col-2 {
    width: 16.66666667%;
  }
  .col-1 {
    width: 8.33333333%;
  }

  .col-pull-12 {
    right: 100%;
  }
  .col-pull-11 {
    right: 91.66666667%;
  }
  .col-pull-10 {
    right: 83.33333333%;
  }
  .col-pull-9 {
    right: 75%;
  }
  .col-pull-8 {
    right: 66.66666667%;
  }
  .col-pull-7 {
    right: 58.33333333%;
  }
  .col-pull-6 {
    right: 50%;
  }
  .col-pull-5 {
    right: 41.66666667%;
  }
  .col-pull-4 {
    right: 33.33333333%;
  }
  .col-pull-3 {
    right: 25%;
  }
  .col-pull-2 {
    right: 16.66666667%;
  }
  .col-pull-1 {
    right: 8.33333333%;
  }
  .col-pull-0 {
    right: auto;
  }
  .col-push-12 {
    left: 100%;
  }
  .col-push-11 {
    left: 91.66666667%;
  }
  .col-push-10 {
    left: 83.33333333%;
  }
  .col-push-9 {
    left: 75%;
  }
  .col-push-8 {
    left: 66.66666667%;
  }
  .col-push-7 {
    left: 58.33333333%;
  }
  .col-push-6 {
    left: 50%;
  }
  .col-push-5 {
    left: 41.66666667%;
  }
  .col-push-4 {
    left: 33.33333333%;
  }
  .col-push-3 {
    left: 25%;
  }
  .col-push-2 {
    left: 16.66666667%;
  }
  .col-push-1 {
    left: 8.33333333%;
  }
  .col-push-0 {
    left: auto;
  }

}

")
