@import edu.kit.hci.soli.dto.LayoutParams
@import edu.kit.hci.soli.config.template.JteContext
@import edu.kit.hci.soli.config.template.PageSpec

@param JteContext context
@param LayoutParams layout

@template.layout(page = new PageSpec("Index", "Soli"), context = context, layout = layout, content = @`
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const calendar = new FullCalendar.Calendar(document.getElementById('calendar'), {
                timeZone: '${context.getTimeZone().getID()}',
                initialView: 'timeGridWeek',
                nowIndicator: true,
                weekends: false,
                allDaySlot: false,
                height: "75vh",
                locale: navigator.language,
                slotLabelFormat: {
                    hour: '2-digit',
                    minute: '2-digit',
                    omitZeroMinute: false
                },
                dayHeaderFormat: {
                    weekday: 'short',
                    day: '2-digit',
                    month: '2-digit',
                    omitCommas: true
                },
                headerToolbar: {
                    left: 'prev,next today',
                    center: 'title',
                    right: 'timeGridWeek,timeGridDay'
                },
                themeSystem: 'bootstrap5',
                selectable: true,
                select: function (info) {
                    window.location.href = "/${layout.getRoom().getId()}/bookings/new"
                        + "?start=" + encodeURIComponent(info.start.toISOString())
                        + "&end=" + encodeURIComponent(info.end.toISOString());
                },
                selectOverlap: function (event) {
                    return !event.classNames.includes('calendar-event-no');
                },
                selectAllow: function (selectInfo) {
                    // Keep these in sync with BookingsController!
                    return selectInfo.start > new Date(new Date().getTime() + 15 * 60 * 1000)
                        && selectInfo.end < new Date(selectInfo.start.getTime() + 4 * 60 * 60 * 1000)
                        && selectInfo.end < new Date(new Date().getTime() + 14 * 24 * 60 * 60 * 1000);
                },
                slotDuration: '00:15:00',
                events: '/api/${layout.getRoom().getId()}/events'
            });

            calendar.render();

            setInterval(() => {
                calendar.refetchEvents();
            }, 1000 * 60 * 5);
        });
    </script>

    <div id="calendar" class="w-full h-5/6"></div>

    <noscript class="w-full">
        <h1 class="text-center">
            ${context.localize("calendar.no_js")}
        </h1>
    </noscript>
`)
