@import edu.kit.hci.soli.config.template.JteContext
@import gg.jte.Content

@param String title
@param Content content
@param JteContext context

<table>
    <tr>
        <td style="padding: 18px;">
            <table class="content" style="border-collapse: collapse; border: 1px solid oklch(var(--n));">
                <tr>
                    <td class="header" style="background-color: oklch(var(--p)); padding: 40px; text-align: center; color: oklch(var(--pc)); font-size: 24px;">
                        ${title}
                    </td>
                </tr>

                <tr>
                    <td class="body" style="background-color: oklch(var(--n)); padding: 40px; text-align: left; color: oklch(var(--nc)); font-size: 16px; line-height: 1.6;">
                        ${content}
                    </td>
                </tr>

                <tr>
                    <td class="footer" style="background-color: oklch(var(--s)); padding: 40px; text-align: center; color: oklch(var(--sc)); font-size: 14px;">
                        ${context.localize("mail.footer.text")} <a href="/">${context.localize("mail.website.name")}</a> <!-- TODO aah wie verlink ich unsere eigene Website?? -->
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>