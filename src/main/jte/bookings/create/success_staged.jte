@import edu.kit.hci.soli.domain.Booking
@import edu.kit.hci.soli.dto.LayoutParams
@import edu.kit.hci.soli.config.template.JteContext
@import edu.kit.hci.soli.config.template.PageSpec

@param JteContext context
@param LayoutParams layout
@param Booking stagedBooking

@template.layout(page = new PageSpec("Booking Staged", "Soli"), context = context, layout = layout, content = @`
    <div class="hero-content text-center">
        <div class="max-w-md">
            <h1 class="text-5xl font-bold">${context.localize("bookings.staged.title")}</h1>
            <p class="py-6">${context.localize("bookings.staged.description")}</p>
            <a class="btn btn-primary" href="/">${context.localize("index.go")}</a>
        </div>
    </div>
`)
