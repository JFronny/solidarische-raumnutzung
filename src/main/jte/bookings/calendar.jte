@import edu.kit.hci.soli.domain.TimeTuple
@import edu.kit.hci.soli.dto.LayoutParams
@import edu.kit.hci.soli.config.template.JteContext
@import edu.kit.hci.soli.config.template.PageSpec
@import java.time.DayOfWeek
@import java.util.Comparator

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
                displayEventTime: false,
                eventContent: function(arg) {
                    let sticky_container = document.createElement('div');
                    sticky_container.className += 'fc-event-title-container';

                    let sticky = document.createElement('div');
                    sticky.className += 'fc-event-title fc-sticky';
                    sticky_container.appendChild(sticky);

                    let icon = document.createElement('i');

                    let arrayOfDomNodes = [ sticky_container, icon]

                    if (arg.event.extendedProps.shareRoomType === 'YES') {
                        icon.className += 'fa-solid fa-users';
                    } else if (arg.event.extendedProps.shareRoomType === 'NO') {
                        icon.className += 'fa-solid fa-users-slash';
                    } else {
                        icon.className += 'fa-solid fa-question';
                        let icon2 = document.createElement('i');
                        icon2.className += 'fa-solid fa-users';
                        arrayOfDomNodes.push(icon2);
                    }

                    return { domNodes: arrayOfDomNodes }
                }
                ,
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
                    const now = (new Date().getTime() + ${context.getTimeZone().getRawOffset()});
                    return selectInfo.start > new Date(now + 15 * 60 * 1000)
                        && selectInfo.end < new Date(selectInfo.start.getTime() + 4 * 60 * 60 * 1000)
                        && selectInfo.end < new Date(now + 14 * 24 * 60 * 60 * 1000);
                },
                selectConstraint: 'businessHours',
                slotDuration: '00:15:00',
                slotMinTime: '${layout.getRoom().getOpeningHours().values().stream().map(TimeTuple::getStart).min(Comparator.naturalOrder()).orElseThrow().toString()}',
                slotMaxTime: '${layout.getRoom().getOpeningHours().values().stream().map(TimeTuple::getEnd).max(Comparator.naturalOrder()).orElseThrow().toString()}',
                eventSources: [
                    '/api/${layout.getRoom().getId()}/events',
                    'https://www.thunderbird.net/media/caldata/autogen/GermanHolidays.ics'
                ],
                businessHours: [
                    <%-- To future readers: a REST endpoint to generate this would be nicer and I know that FullCalendar supports this. --%>
                    <%-- Unfortunately, using that appears to require some form of premium subscription, so this will have to do. --%>
                    @for (var day : DayOfWeek.values())
                    @if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY)
                    !{var tuple = layout.getRoom().getOpeningHours().get(day);}
                    {
                        daysOfWeek: [${day.getValue()}],
                        startTime: '${tuple.getStart().toString()}',
                        endTime: '${tuple.getEnd().toString()}'
                    },
                    @endif
                    @endfor
                ]
            });

            calendar.render();
            setInterval(() => {
                calendar.refetchEvents();
            }, 1000 * 60 * 5);
        });
    </script>

    <div id="calendar" class="w-full"></div>

    <noscript class="w-full">
        <h1 class="text-center">
            ${context.localize("calendar.no_js")}
        </h1>
    </noscript>
`)
