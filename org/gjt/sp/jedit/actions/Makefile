#
# Makefile for org.gjt.sp.jedit.actions package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/actions/about.class \
	org/gjt/sp/jedit/actions/block_comment.class \
	org/gjt/sp/jedit/actions/box_comment.class \
	org/gjt/sp/jedit/actions/buffer_options.class \
	org/gjt/sp/jedit/actions/clear.class \
	org/gjt/sp/jedit/actions/clear_marker.class \
	org/gjt/sp/jedit/actions/close_file.class \
	org/gjt/sp/jedit/actions/close_view.class \
	org/gjt/sp/jedit/actions/copy.class \
	org/gjt/sp/jedit/actions/cut.class \
	org/gjt/sp/jedit/actions/delete_end_line.class \
	org/gjt/sp/jedit/actions/delete_line.class \
	org/gjt/sp/jedit/actions/delete_paragraph.class \
	org/gjt/sp/jedit/actions/delete_start_line.class \
	org/gjt/sp/jedit/actions/exchange_anchor.class \
	org/gjt/sp/jedit/actions/exit.class \
	org/gjt/sp/jedit/actions/expand_abbrev.class \
	org/gjt/sp/jedit/actions/find.class \
	org/gjt/sp/jedit/actions/find_next.class \
	org/gjt/sp/jedit/actions/find_selection.class \
	org/gjt/sp/jedit/actions/format.class \
	org/gjt/sp/jedit/actions/global_options.class \
	org/gjt/sp/jedit/actions/goto_anchor.class \
	org/gjt/sp/jedit/actions/goto_end_indent.class \
	org/gjt/sp/jedit/actions/goto_line.class \
	org/gjt/sp/jedit/actions/goto_marker.class \
	org/gjt/sp/jedit/actions/help.class \
	org/gjt/sp/jedit/actions/hypersearch.class \
	org/gjt/sp/jedit/actions/hypersearch_selection.class \
	org/gjt/sp/jedit/actions/indent_on_enter.class \
	org/gjt/sp/jedit/actions/indent_on_tab.class \
	org/gjt/sp/jedit/actions/insert_date.class \
	org/gjt/sp/jedit/actions/join_lines.class \
	org/gjt/sp/jedit/actions/locate_bracket.class \
	org/gjt/sp/jedit/actions/multifile_search.class \
	org/gjt/sp/jedit/actions/new_file.class \
	org/gjt/sp/jedit/actions/new_view.class \
	org/gjt/sp/jedit/actions/next_buffer.class \
	org/gjt/sp/jedit/actions/next_paragraph.class \
	org/gjt/sp/jedit/actions/open_file.class \
	org/gjt/sp/jedit/actions/open_path.class \
	org/gjt/sp/jedit/actions/open_selection.class \
	org/gjt/sp/jedit/actions/open_url.class \
	org/gjt/sp/jedit/actions/paste.class \
	org/gjt/sp/jedit/actions/paste_predefined.class \
	org/gjt/sp/jedit/actions/paste_previous.class \
	org/gjt/sp/jedit/actions/plugin_options.class \
	org/gjt/sp/jedit/actions/prev_buffer.class \
	org/gjt/sp/jedit/actions/prev_paragraph.class \
	org/gjt/sp/jedit/actions/print.class \
	org/gjt/sp/jedit/actions/redo.class \
	org/gjt/sp/jedit/actions/reload.class \
	org/gjt/sp/jedit/actions/replace_all.class \
	org/gjt/sp/jedit/actions/replace_in_selection.class \
	org/gjt/sp/jedit/actions/replace_next.class \
	org/gjt/sp/jedit/actions/save.class \
	org/gjt/sp/jedit/actions/save_all.class \
	org/gjt/sp/jedit/actions/save_as.class \
	org/gjt/sp/jedit/actions/save_url.class \
	org/gjt/sp/jedit/actions/scroll_line.class \
	org/gjt/sp/jedit/actions/select_all.class \
	org/gjt/sp/jedit/actions/select_anchor.class \
	org/gjt/sp/jedit/actions/select_block.class \
	org/gjt/sp/jedit/actions/select_buffer.class \
	org/gjt/sp/jedit/actions/select_line_range.class \
	org/gjt/sp/jedit/actions/select_next_paragraph.class \
	org/gjt/sp/jedit/actions/select_prev_paragraph.class \
	org/gjt/sp/jedit/actions/send.class \
	org/gjt/sp/jedit/actions/set_anchor.class \
	org/gjt/sp/jedit/actions/set_marker.class \
	org/gjt/sp/jedit/actions/shift_left.class \
	org/gjt/sp/jedit/actions/shift_right.class \
	org/gjt/sp/jedit/actions/tab.class \
	org/gjt/sp/jedit/actions/to_lower.class \
	org/gjt/sp/jedit/actions/to_upper.class \
	org/gjt/sp/jedit/actions/undo.class \
	org/gjt/sp/jedit/actions/untab.class \
	org/gjt/sp/jedit/actions/wing_comment.class \
	org/gjt/sp/jedit/actions/word_count.class

others='org/gjt/sp/jedit/actions/print$$PrintTabExpander.class'

all: $(classes)
	echo $(classes) $(others) >> .resources

include Rules.make
