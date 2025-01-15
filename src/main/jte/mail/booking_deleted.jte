@import edu.kit.hci.soli.config.template.JteContext
@import edu.kit.hci.soli.domain.Booking
@import edu.kit.hci.soli.dto.BookingDeleteReason

@param JteContext context

@param Booking booking
@param BookingDeleteReason reason

@template.mail.layout(title= context.lookup("mail.booking_deleted.heading"), content = @`
    <p>
        ${context.localize("mail.booking_deleted.message", booking.getUser().getUsername(), booking.getRoom().getId(), booking.getStartDate().toString(), (context.localize("bookings.delete_reason." + reason)))} <br>
        ${context.localize("mail.ending")}
    </p>
    <div style="text-align:center">
        <button class="btn btn-accent">
            <a href= "${context.getHostname()}${booking.getRoom().getId()}/bookings/${booking.getId()}/collaboration">
                <p>${context.localize("mail.make_new_booking")}</p>
            </a>
        </button>
    </div>
`, context = context)
