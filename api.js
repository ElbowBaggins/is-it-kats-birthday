var express = require('express');
var router = express.Router();

var KatsBirthday = require("./public/js/katsbirthday"),
    zones = require("./public/js/zones"),
    labels = require("./public/js/labels")
    moment = require("moment-timezone");

module.exports = function(app, config, findCountry) {

    var ERROR = function(message) {
        return {
            "errors": [
                {"message": message}
            ]
        }
    };

    var UNAUTHORIZED = "No thank you.",
        BAD_TRIGGER = "Invalid trigger.",
        BAD_TRIGGER_FIELD = "Invalid trigger field.",
        BAD_TIMEZONE = "Invalid timezone.",
        BAD_COUNTRY = "Invalid country code.",
        INVALID_TIMEZONE = "Not a valid timezone.",
        MISSING_TRIGGER_FIELDS = "Missing trigger fields.",
        EVERYTHING_IS_FINE = "Everything is fine."


    // if a client secret is set, gate access to some methods
    var authed = function(req) {
        var secret = config.ifttt;
        if (!secret) {
            console.log("No client secret set, not checking auth.");
            return true;
        }

        return req.get("IFTTT-Channel-Key") == secret;
    };

    // Every KatsBirthday since 2007, in the given time zone,
    // to honor the site's history.
    var katsBirthdaysFor = function(country, timezone) {
        var firstYear = 2007; // nostalgia

        var now = new Date();

        var thisYear = 1900 + now.getYear();
        var years = thisYear - firstYear;

        var katsBirthdays = [];

        for (var i=0; i<=years; i++) {
            var year = thisYear - i; // reverse chronological order
            var katsBirthdayDay = KatsBirthday.forYear(moment, year, timezone);

            // only show katsBirthdayes past
            if (now > katsBirthdayDay) {
                katsBirthdays.push({
                    answer: KatsBirthday.yes(country),
                    katsBirthday: true,
                    katsBirthday_day: katsBirthdayDay.toISOString(),
                    katsBirthday_time: (katsBirthdayDay.getTime() / 1000),
                    year: year,
                    id: "katsBirthday-" + year,
                    timezone: timezone,
                    country: country,
                    country_names: KatsBirthday.countries[country].names
                });
            }
        }

        return katsBirthdays;
    };

    // given a katsBirthday object above, prepare it for IFTTT format
    var iftttFor = function(katsBirthday) {
        return {
            answer: katsBirthday.answer,
            year: katsBirthday.year,
            created_at: katsBirthday.katsBirthday_day,
            meta: {
                // should only ever fire once a year
                id: katsBirthday.id,
                timestamp: katsBirthday.katsBirthday_time,
            }
        }
    };

    // isitkatsBirthday API.
    // Default to Esperanto and Greenwich Mean Time.
    router.get('/', function(req, res) {
        var country = findCountry(req) || "EO";
        if (!KatsBirthday.countries[country]) return res.status(400).json(ERROR(BAD_COUNTRY));

        var timezone = (req.query.timezone || "UTC");
        if (moment.tz.zone(timezone) == null) {
            return res.status(400).json(ERROR(BAD_TIMEZONE));
        }

        res.json({katsBirthdays: katsBirthdaysFor(country, timezone)})
    });

    router.get('/test', function(req, res) {
        res.send("The API is alive!");
    });

    router.post('/ifttt/v1/triggers/:trigger', function(req, res) {
        if (config.ifttt_debug) console.log("trigger: " + req.params.trigger);
        if (!authed(req)) return res.status(401).json(ERROR(UNAUTHORIZED));

        if (req.params.trigger != "katsBirthday") return res.status(400).json(ERROR(BAD_TRIGGER));

        if (!req.body.triggerFields || !req.body.triggerFields.timezone) return res.status(400).json(ERROR(MISSING_TRIGGER_FIELDS));
        if (!zones[req.body.triggerFields.timezone]) return res.status(400).json(ERROR(INVALID_TIMEZONE));

        if (req.body.limit === 0)
            res.json({"data": []});
        else {

            // map IFTTT zone name to standard zone name
            // we've validated that this field is here
            var timezone = zones[req.body.triggerFields.timezone];

            // every katsBirthday since 2007 in the user's time zone
            // for IFTTT, sadly I must just assume English
            var katsBirthdays = katsBirthdaysFor("US", timezone);

            var items = [];
            for (var i=0; i<katsBirthdays.length; i++)
                items.push(iftttFor(katsBirthdays[i]))

            // no more than 20 years of katsBirthday data, tops
            items = items.slice(0, (req.body.limit || 20));

            res.json({"data": items});
        }
    });

    router.post('/ifttt/v1/triggers/:trigger/fields/:field/options', function(req, res) {
        if (!authed(req)) return res.status(401).json(ERROR(UNAUTHORIZED));
        if (config.ifttt_debug) console.log('triggerfield, trigger: ' + req.params.trigger + ', field: ' + req.params.field);
        if (req.params.trigger != "katsBirthday") return res.status(400).json(ERROR(BAD_TRIGGER));
        if (req.params.field != "timezone") return res.status(400).json(ERROR(BAD_TRIGGER_FIELD));

        res.json({"data": labels});
    });

    router.post('/ifttt/v1/triggers/:trigger/fields/:field/validate', function(req, res) {
        if (!authed(req)) return res.status(401).json(ERROR(UNAUTHORIZED));
        if (config.ifttt_debug) console.log('triggerfield validate, trigger: ' + req.params.trigger + ', field: ' + req.params.field);
        if (req.params.trigger != "katsBirthday") return res.status(400).json(ERROR(BAD_TRIGGER));
        if (req.params.field != "timezone") return res.status(400).json(ERROR(BAD_TRIGGER_FIELD));

        if (zones[req.body.value])
            res.json({"data": {valid: true}});
        else
            res.json({"data": {valid: false, message: INVALID_TIMEZONE}});

    });

    router.get('/ifttt/v1/status', function(req, res) {
        if (!authed(req)) return res.status(401).json(ERROR(UNAUTHORIZED));

        res.json({
            "data": {
                "status": "OK",
                "time": new Date().toISOString(),
                "message": EVERYTHING_IS_FINE
            }
        });
    });

    router.post('/ifttt/v1/test/setup', function(req, res) {
        if (!authed(req)) return res.status(401).json(ERROR(UNAUTHORIZED));
        res.json({
            "data": {
                "samples": {
                    "triggers": {
                        "katsBirthday": {
                            "timezone": "Pacific Time (US & Canada)"
                        }
                    },
                    "triggerFieldValidations": {
                        "katsBirthday": {
                            "timezone": {
                                "valid": "Pacific Time (US & Canada)",
                                "invalid": "America/Los_Angeles"
                            }
                        }
                    }
                }
            }
        });
    });

    return router;

};
