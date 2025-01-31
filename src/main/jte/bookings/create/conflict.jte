@import edu.kit.hci.soli.domain.Booking
@import edu.kit.hci.soli.dto.BookingAttemptResult
@import edu.kit.hci.soli.dto.LayoutParams
@import edu.kit.hci.soli.config.template.JteContext
@import edu.kit.hci.soli.config.template.PageSpec
@import org.springframework.security.web.csrf.CsrfToken
@import static org.springframework.http.HttpMethod.*

@param JteContext context
@param LayoutParams layout
@param CsrfToken csrf

@param Booking attemptedBooking
@param BookingAttemptResult.PossibleCooperation bookingResult

@template.layout(page = new PageSpec("Conflict", "Soli"), context = context, layout = layout, content = @`
    <div class="hero-content text-center">
        <div class="max-w-md">
            <h1 class="text-5xl font-bold">Konfliktlösung</h1>
            <p class="py-6">
                Ein lösbarer Konflikt wurde festgestellt.<br>
                Vorgeschlagene Lösung:
            </p>
            <ul>
                <!-- TODO: Diese list-items werden nicht korrekt dargestellt -->
                <!-- TODO: Localize! -->
                @for(Booking booking : bookingResult.contact())
                    <li>Kontaktiere: ${booking.getDescription()} (${context.format(booking.getStartDate())} - ${context.format(booking.getEndDate())})</li>
                @endfor
                @for(Booking booking : bookingResult.override())
                    <li>Lösche: ${booking.getDescription()} (${context.format(booking.getStartDate())} - ${context.format(booking.getEndDate())})</li>
                @endfor
                @for(Booking booking : bookingResult.cooperate())
                    <li>Nutze gleichzeitig mit: ${booking.getDescription()} (${context.format(booking.getStartDate())} - ${context.format(booking.getEndDate())})</li>
                @endfor
            </ul>
            @template.util.form(csrf = csrf, action = "/" + attemptedBooking.getRoom().getId() + "/bookings/new/conflict", method = POST, content = @`
                <button class="btn btn-primary" type="submit">Lösen</button>
            `)
        </div>
    </div>
`)