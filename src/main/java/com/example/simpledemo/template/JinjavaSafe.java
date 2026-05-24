package com.example.simpledemo.template;

/**
 * Escapes text embedded in Jinjava templates via {@code {{ variable }}}.
 *
 * <p>Git diffs often contain {@code {% ... %}}, {@code {{ }}, or {@code %}} from
 * source files (e.g. {@code .jinja} templates). Jinjava re-parses variable values,
 * which causes {@code TemplateSyntaxException}. Use fullwidth look-alike characters
 * so the LLM still reads the diff clearly but Jinjava does not interpret tags.
 */
public final class JinjavaSafe {

  private JinjavaSafe() {
  }

  public static String escape(String text) {
    if (text == null || text.isEmpty()) {
      return text == null ? "" : text;
    }
    return text
        .replace("{{", "｛｛")
        .replace("}}", "｝｝")
        .replace("{%", "｛%")
        .replace("%}", "%｝");
  }
}
