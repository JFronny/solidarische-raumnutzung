@import edu.kit.hci.soli.config.template.JteContext
@import gg.jte.Content

@param String title
@param Content content
@param JteContext context
@param String css

<style>
    ${css}
</style>
<table>
    <tr>
        <td>
            <table class="content">
                <tr class="header">
                    <td>
                        <div class="w-20 rounded-full">
                            <img src="/favicon.svg">
                        </div>
                        ${title}
                    </td>
                </tr>

                <tr class="body">
                    <td>
                        ${content}
                    </td>
                </tr>

                <tr class="footer">
                    <td>
                        ${context.localize("mail.footer.text")} <a href="/">${context.localize("mail.website.name")}</a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>