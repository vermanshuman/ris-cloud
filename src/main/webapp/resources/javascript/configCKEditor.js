/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights
 *          reserved. For licensing, see LICENSE.md or
 *          http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function(config) {
	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	// config.uiColor = '#AADC6E';

	config.fontSize_defaultLabel = '18px';
	config.contentsCss = 'CKEditorStyle.css';
	
	config.enterMode = CKEDITOR.ENTER_BR;
	config.disableNativeSpellChecker = false;

	config.extraPlugins = 'zoom';
	config.versionCheck = false;
	config.language = 'it_IT';
	config.wsc_lang = 'it_IT'; 
	config.scayt_sLang = 'it_IT';
	config.scayt_defLan ='it_IT';
	config.defaultLanguage='it_IT';
	config.scayt_autoStartup = false;
	config.removePlugins = 'tabletools,tableselection,liststyle,contextmenu';
	config.toolbar = [
			{
				name : 'basicstyles',
				groups : [ 'basicstyles', 'cleanup' ],
				items : [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript',
						'Superscript', '-', 'RemoveFormat' ]
			},
			{
				name : 'paragraph',
				groups : [ 'list', 'indent', 'blocks', 'align', 'bidi' ],
				items : [ 'NumberedList', 'BulletedList', '-', 'Outdent',
						'Indent', '-', '-', 'JustifyLeft', 'JustifyCenter',
						'JustifyRight', 'JustifyBlock', '-' ]
			},
			{
				name : 'insert',
				items : [ 'Image', 'Table', 'HorizontalRule', 'SpecialChar' ]
			},
			{
				name : 'styles',
				items : [ 'Font', 'FontSize' ]
			},
			{
				name : 'colors',
				items : [ 'TextColor', 'BGColor' ]
			},
			{
				name : 'clipboard',
				groups : [ 'clipboard', 'undo' ],
				items : [ 'Cut', 'Copy', 'PasteText', 'PasteFromWord', '-',
						'Undo', 'Redo' ]
			}, {
				name : 'tools',
				items : [ 'Maximize' ]
			}, {
				name : 'editing',
				groups : [ 'selection', 'spellchecker' ],
				items : [ 'Replace', '-', 'SelectAll', '-', 'foundeospellchecker' ]

			}, {
				name : 'source',
				items : [ 'Source' ]
			}, {
				name : 'Zoom',
				items : [ 'Zoom' ]
			} ];
};
CKEDITOR.on("instanceReady", function(event) {
	event.editor.on("beforeCommandExec", function(event) {
		// Show the paste dialog for the paste buttons and right-click paste
		if (event.data.name == "paste") {
			event.editor._.forcePasteDialog = true;
		}
		// Don't show the paste dialog for Ctrl+Shift+V
		if (event.data.name == "pastetext" && event.data.commandData != null && event.data.commandData.from == "keystrokeHandler") {
			event.cancel();
		}
	})
});